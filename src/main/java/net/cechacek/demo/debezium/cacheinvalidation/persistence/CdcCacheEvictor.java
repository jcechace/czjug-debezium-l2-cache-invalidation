package net.cechacek.demo.debezium.cacheinvalidation.persistence;

import io.debezium.config.Configuration;
import io.debezium.connector.postgresql.PostgresConnector;
import io.debezium.connector.postgresql.PostgresConnectorConfig;
import io.debezium.embedded.Connect;
import io.debezium.embedded.EmbeddedEngineConfig;
import io.debezium.embedded.async.ConvertingAsyncEngineBuilderFactory;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.KeyValueHeaderChangeEventFormat;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import net.cechacek.demo.debezium.cacheinvalidation.model.Item;
import net.cechacek.demo.debezium.cacheinvalidation.persistence.config.CdcConfig;
import net.cechacek.demo.debezium.cacheinvalidation.persistence.config.DataSourceConfig;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.jboss.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Listens to database changes using Debezium's embedded engine. If a change
 * event for an {@link Item} arrives that has not been caused by this
 * application itself, that {@code Item} will be removed from the JPA 2nd-level
 * cache.
 */
@ApplicationScoped
@Startup
public class CdcCacheEvictor {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DebeziumEngine<?> engine;

    @Inject
    Logger logger;
    @Inject
    DataSourceConfig dsConfig;
    @Inject
    CdcConfig cdcConfig;
    @Inject
    KnownTransactions knownTransactions;
    @Inject
    @PersistenceContext
    EntityManagerFactory emf;


    @PostConstruct
    public void start() {
        // Construct Debezium config
        var connection = dsConfig.connectionConfig();
        var config = Configuration.create()
                .with(EmbeddedEngineConfig.ENGINE_NAME, cdcConfig.name())
                .with(EmbeddedEngineConfig.CONNECTOR_CLASS, PostgresConnector.class.getName())
                .with(EmbeddedEngineConfig.OFFSET_STORAGE, MemoryOffsetBackingStore.class)
                .with(PostgresConnectorConfig.TOPIC_PREFIX, cdcConfig.prefix())
                .with(PostgresConnectorConfig.HOSTNAME, connection.host())
                .with(PostgresConnectorConfig.PORT, connection.port())
                .with(PostgresConnectorConfig.USER, connection.username())
                .with(PostgresConnectorConfig.PASSWORD, connection.password())
                .with(PostgresConnectorConfig.DATABASE_NAME, connection.database())
                .with(PostgresConnectorConfig.PLUGIN_NAME, PostgresConnectorConfig.LogicalDecoder.PGOUTPUT.getValue())
                .with(PostgresConnectorConfig.INCLUDE_SCHEMA_CHANGES, false)
                .with(PostgresConnectorConfig.TABLE_INCLUDE_LIST, cdcConfig.tablesAsString())
                .with(PostgresConnectorConfig.SKIPPED_OPERATIONS, "t,c")
                .build();

        // Create Debezium engine
        logger.info("Creating Debezium engine");
        var format = KeyValueHeaderChangeEventFormat.of(Connect.class, Connect.class, Connect.class);
        var factory = ConvertingAsyncEngineBuilderFactory.class.getName();

        this.engine = DebeziumEngine.create(format, factory)
                .using(config.asProperties())
                .notifying(this::handleDbChangeEvent)
                .build();

        // Start Debezium engine
        logger.info("Attempting to start debezium engine");
        executor.execute(engine);
    }

    public void stop(@Observes ShutdownEvent event) {
        // Stop the engine
        try {
            logger.info("Attempting to stop Debezium");
            engine.close();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Exception while shutting down Debezium", e);
        }

    }

    private void handleDbChangeEvent(ChangeEvent<SourceRecord, SourceRecord> event) {
        // Process Data Change event
        var record = event.value();
        logger.info("Handling DB change event " + record);

        if (record.topic().equals(cdcConfig.topic("public.item"))) {

            var itemId = ((Struct) record.key()).getInt64("id");
            var payload = (Struct) record.value();
            var txId = ((Struct) payload.get("source")).getInt64("txId");

            if (knownTransactions.isKnown(txId)) {
                logger.infof("Not evicting item %d from 2nd-level cache as TX %d was started by this application", itemId, txId);
            } else {
                logger.infof("Evicting item %d from 2nd-level cache as TX %d was not started by this application", itemId, txId);
                emf.getCache().evict(Item.class, itemId);
            }
        }
    }
}

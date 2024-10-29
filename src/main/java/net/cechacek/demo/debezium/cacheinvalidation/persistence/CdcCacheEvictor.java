package net.cechacek.demo.debezium.cacheinvalidation.persistence;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
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
import org.apache.kafka.connect.source.SourceRecord;
import org.jboss.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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


        // Create Debezium engine


        // Start Debezium engine

    }

    public void stop(@Observes ShutdownEvent event) {
        // Stop the engine

    }

    private void handleDbChangeEvent(ChangeEvent<SourceRecord, SourceRecord> event) {
        // Process Data Change event

    }
}

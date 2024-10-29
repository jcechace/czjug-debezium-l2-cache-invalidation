package net.cechacek.demo.debezium.cacheinvalidation.persistence;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import static java.util.concurrent.CompletableFuture.completedFuture;

@ApplicationScoped
@Startup
public class KnownTransactions {

    @Inject
    Logger logger;

    @CacheName("application-transactions")
    Cache applicationTransactions;

    public void register(long txId) {
        logger.infof("Registering TX %d started by this application", txId);
        applicationTransactions.as(CaffeineCache.class)
                .put(txId, completedFuture(true));
    }

    public boolean isKnown(long txId) {
        return applicationTransactions.as(CaffeineCache.class)
                .getIfPresent(txId) != null;
    }
}

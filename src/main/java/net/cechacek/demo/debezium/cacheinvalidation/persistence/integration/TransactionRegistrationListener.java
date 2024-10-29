/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.cechacek.demo.debezium.cacheinvalidation.persistence.integration;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import net.cechacek.demo.debezium.cacheinvalidation.persistence.KnownTransactions;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Hibernate event listener obtains the current TX id and stores it in a cache.
 */
@ApplicationScoped
@Startup
public class TransactionRegistrationListener implements FlushEventListener {

    /**
     * Keep track of active sessions
     */
    private final ConcurrentMap<Session, Boolean> sessionsWithBeforeTransactionCompletion;
    private final KnownTransactions knownTransactions;

    public TransactionRegistrationListener(KnownTransactions knownTransactions) {
        this.sessionsWithBeforeTransactionCompletion = new ConcurrentHashMap<>();
        this.knownTransactions = knownTransactions;
    }

    @Override
    public void onFlush(FlushEvent event) throws HibernateException {
        // If the session is already registered, we don't need to do it again
        if (sessionsWithBeforeTransactionCompletion.containsKey(event.getSession())) {
            return;
        }
        // If not, remember the session until the transaction is completed
        sessionsWithBeforeTransactionCompletion.put(event.getSession(), true);
        event.getSession().getActionQueue().registerProcess(this::registerTxId);
    }

    /**
     * Register the current transaction ID.
     * @param session the session
     */
    private void registerTxId(Session session) {
        var txId = session.createNativeQuery("SELECT txid_current()", Long.class)
                .setHibernateFlushMode(FlushMode.MANUAL)
                .getSingleResult();
        knownTransactions.register(txId);

        sessionsWithBeforeTransactionCompletion.remove(session);
    }
}

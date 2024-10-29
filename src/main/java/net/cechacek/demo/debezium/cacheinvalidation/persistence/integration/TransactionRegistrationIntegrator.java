package net.cechacek.demo.debezium.cacheinvalidation.persistence.integration;

import jakarta.enterprise.inject.spi.CDI;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.jboss.logging.Logger;

public class TransactionRegistrationIntegrator implements Integrator {

    private static final Logger LOG = Logger.getLogger(TransactionRegistrationIntegrator.class);

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
                          SessionFactoryImplementor sessionFactory) {
        LOG.info("TransactionRegistrationIntegrator#integrate()");

        sessionFactory.getServiceRegistry()
                .getService(EventListenerRegistry.class)
                .appendListeners(EventType.FLUSH, getTransactionRegistrationListener());
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

    private TransactionRegistrationListener getTransactionRegistrationListener() {
        return CDI.current().select(TransactionRegistrationListener.class).get();
    }
}

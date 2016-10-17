package eu.domibus.taskexecutor.wildfly;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class DomibusExecutorServiceFactory implements FactoryBean<ManagedExecutorService> {

    private static final Log LOGGER = LogFactory.getLog(DomibusExecutorServiceFactory.class);

    public static final String DEFAULT_EXECUTOR_SERVICE = "java:jboss/ee/concurrency/executor/default";

    protected String executorServiceJndiName;

    protected ManagedExecutorService lookupExecutorService(String jndiName) {
        ManagedExecutorService result = null;
        try {
            result = InitialContext.doLookup(jndiName);
        } catch (NamingException e) {
            LOGGER.warn("Failed to lookup executor service: " + jndiName);
        }
        return result;
    }

    protected ManagedExecutorService getDefaultWorkManager() {
        ManagedExecutorService result = lookupExecutorService(DEFAULT_EXECUTOR_SERVICE);
        LOGGER.debug("Default executor service: " + DEFAULT_EXECUTOR_SERVICE + " = " + result);
        return result;
    }

    protected ManagedExecutorService getGlobalWorkManager() {
        ManagedExecutorService result = lookupExecutorService(executorServiceJndiName);
        LOGGER.debug("Global executor service: " + executorServiceJndiName + " = " + result);
        return result;
    }

    @Override
    public ManagedExecutorService getObject() throws Exception {
        ManagedExecutorService result = null;

        result = getGlobalWorkManager();
        if (result != null) {
            LOGGER.debug("Using global executor service: " + result);
            return result;
        }

        result = getDefaultWorkManager();
        if (result != null) {
            LOGGER.debug("Using global executor service: " + result);
            return result;
        }

        LOGGER.error("Failed to get executor service");
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return ManagedExecutorService.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public void setExecutorServiceJndiName(String executorServiceJndiName) {
        this.executorServiceJndiName = executorServiceJndiName;
    }
}

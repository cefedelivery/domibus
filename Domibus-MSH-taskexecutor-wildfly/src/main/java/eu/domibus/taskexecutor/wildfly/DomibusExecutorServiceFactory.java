package eu.domibus.taskexecutor.wildfly;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Cosmin Baciu
 * @since 3.3
 *
 * This class is responsible for discovering a {@link ManagedExecutorService} from the JNDI tree
 * It checks first in the JNDI tree if there is a configured application executor service. If it fails to get it it falls back to the default executor.
 */

public class DomibusExecutorServiceFactory implements FactoryBean<ManagedExecutorService> {

    private static final Log LOG = LogFactory.getLog(DomibusExecutorServiceFactory.class);

    public static final String DEFAULT_EXECUTOR_SERVICE = "java:jboss/ee/concurrency/executor/default";

    protected String executorServiceJndiName;

    protected ManagedExecutorService lookupExecutorService(String jndiName) {
        ManagedExecutorService result = null;
        try {
            result = InitialContext.doLookup(jndiName);
        } catch (NamingException e) {
            LOG.warn("Failed to lookup executor service: " + jndiName);
        }
        return result;
    }

    protected ManagedExecutorService getDefaultExecutorService() {
        ManagedExecutorService result = lookupExecutorService(DEFAULT_EXECUTOR_SERVICE);
        LOG.debug("Default executor service: " + DEFAULT_EXECUTOR_SERVICE + " = " + result);
        return result;
    }

    protected ManagedExecutorService getGlobalExecutorService() {
        ManagedExecutorService result = lookupExecutorService(executorServiceJndiName);
        LOG.debug("Global executor service: " + executorServiceJndiName + " = " + result);
        return result;
    }

    @Override
    public ManagedExecutorService getObject() throws Exception {
        ManagedExecutorService result = null;

        result = getGlobalExecutorService();
        if (result != null) {
            LOG.debug("Using global executor service: " + result);
            return result;
        }

        result = getDefaultExecutorService();
        if (result != null) {
            LOG.debug("Using global executor service: " + result);
            return result;
        }

        LOG.error("Failed to get executor service");
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

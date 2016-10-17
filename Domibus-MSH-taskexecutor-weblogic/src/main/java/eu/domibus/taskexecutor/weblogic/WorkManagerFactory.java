package eu.domibus.taskexecutor.weblogic;

import commonj.work.WorkManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class WorkManagerFactory implements FactoryBean<WorkManager> {

    private static final Log LOGGER = LogFactory.getLog(WorkManagerFactory.class);

    public static final String DEFAULT_WORK_MANAGER = "java:comp/env/wm/default";

    protected String workManagerJndiName;

    protected WorkManager lookupWorkManager(String jndiName) {
        WorkManager result = null;
        try {
            result = InitialContext.doLookup(jndiName);
        } catch (NamingException e) {
            LOGGER.warn("Failed to lookup work manager: " + jndiName);
        }
        return result;
    }

    protected WorkManager getDefaultWorkManager() {
        WorkManager result = lookupWorkManager(DEFAULT_WORK_MANAGER);
        LOGGER.debug("Default work manager: " + DEFAULT_WORK_MANAGER + " = " + result);
        return result;
    }

    protected WorkManager getGlobalWorkManager() {
        WorkManager result = lookupWorkManager(workManagerJndiName);
        LOGGER.debug("Global work manager: " + workManagerJndiName + " = " + result);
        return result;
    }

    @Override
    public WorkManager getObject() throws Exception {
        WorkManager result = null;

        result = getGlobalWorkManager();
        if (result != null) {
            LOGGER.debug("Using global work manager: " + result);
            return result;
        }

        result = getDefaultWorkManager();
        if (result != null) {
            LOGGER.debug("Using global work manager: " + result);
            return result;
        }

        LOGGER.error("Failed to get work manager");
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return WorkManager.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public String getWorkManagerJndiName() {
        return workManagerJndiName;
    }

    public void setWorkManagerJndiName(String workManagerJndiName) {
        this.workManagerJndiName = workManagerJndiName;
    }
}

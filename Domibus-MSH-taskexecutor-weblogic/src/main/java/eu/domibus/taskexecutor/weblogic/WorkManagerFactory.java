package eu.domibus.taskexecutor.weblogic;

import commonj.work.WorkManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Cosmin Baciu
 * @since 3.3
 *
 * This class is responsible for discovering a {@link WorkManager} from the JNDI tree
 * It checks first in the JNDI tree if there is a configured application work manager. If it fails to get it it falls back to the default work manager.
 */
public class WorkManagerFactory implements FactoryBean<WorkManager> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WorkManagerFactory.class);

    public static final String DEFAULT_WORK_MANAGER = "java:comp/env/wm/default";

    protected String workManagerJndiName;

    protected WorkManager lookupWorkManager(String jndiName) {
        WorkManager result = null;
        try {
            result = InitialContext.doLookup(jndiName);
        } catch (NamingException e) {
            LOG.debug("Failed to lookup work manager: " + jndiName, e);
        }
        return result;
    }

    protected WorkManager getDefaultWorkManager() {
        WorkManager result = lookupWorkManager(DEFAULT_WORK_MANAGER);
        LOG.debug("Default work manager: " + DEFAULT_WORK_MANAGER + " = " + result);
        return result;
    }

    protected WorkManager getGlobalWorkManager() {
        WorkManager result = lookupWorkManager(workManagerJndiName);
        LOG.debug("Global work manager: " + workManagerJndiName + " = " + result);
        return result;
    }

    @Override
    public WorkManager getObject() throws Exception {
        WorkManager result = null;

        result = getGlobalWorkManager();
        if (result != null) {
            LOG.debug("Using global work manager: " + result);
            return result;
        }

        result = getDefaultWorkManager();
        if (result != null) {
            LOG.debug("Using global work manager: " + result);
            return result;
        }

        LOG.error("Failed to get work manager");
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

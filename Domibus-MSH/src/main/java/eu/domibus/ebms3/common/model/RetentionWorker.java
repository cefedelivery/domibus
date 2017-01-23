package eu.domibus.ebms3.common.model;

import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.ebms3.security.util.AuthUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

//TODO move this class out of the model package
/**
 * @author Christian Koch, Stefan Mueller
 */
@DisallowConcurrentExecution
public class RetentionWorker extends QuartzJobBean {


    private static final Log LOG = LogFactory.getLog(RetentionWorker.class);


    @Autowired
    private MessageRetentionService messageRetentionService;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    AuthUtils authUtils;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        LOG.debug("RetentionWorker executed");
        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retention_user", "retention_password");
        }

        if (configurationDAO.configurationExists()) {
            messageRetentionService.deleteExpiredMessages();
        }
    }
}

package eu.domibus.ebms3.common.model;

import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;


/**
 * @author Christian Koch, Stefan Mueller
 */
@DisallowConcurrentExecution
public class RetentionWorker extends QuartzJobBean {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetentionWorker.class);


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

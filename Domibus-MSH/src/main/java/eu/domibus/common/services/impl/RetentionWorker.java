package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Christian Koch, Stefan Mueller
 */
@DisallowConcurrentExecution
public class RetentionWorker extends DomibusQuartzJobBean {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetentionWorker.class);


    @Autowired
    private MessageRetentionService messageRetentionService;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    AuthUtils authUtils;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        LOG.debug("RetentionWorker executed");
        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retention_user", "retention_password");
        }

        if (configurationDAO.configurationExists()) {
            messageRetentionService.deleteExpiredMessages();
        }
    }
}

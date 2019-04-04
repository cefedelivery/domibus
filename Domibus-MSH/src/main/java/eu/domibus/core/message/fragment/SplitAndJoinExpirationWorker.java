package eu.domibus.core.message.fragment;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@DisallowConcurrentExecution
public class SplitAndJoinExpirationWorker extends DomibusQuartzJobBean {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SplitAndJoinExpirationWorker.class);

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    private AuthUtils authUtils;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        LOG.debug("SplitAndJoinExpirationWorker executed");

        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("splitAndJoinExpiration_user", "splitAndJoinExpiration_password");
        }

        if (configurationDAO.configurationExists()) {
            LOG.debug("Could not checked for expired SplitAndJoin messages: PMode is not configured");
            return;
        }

        splitAndJoinService.handleExpiredMessages();
    }
}

package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.quartz.DomibusQuartzJobExtBean;
import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Job to launch dss refresh mechanism.
 */
public class DssRefreshWorker extends DomibusQuartzJobExtBean {

    private static final Logger LOG = LoggerFactory.getLogger(DssRefreshWorker.class);

    @Autowired
    private TSLValidationJob tslValidationJob;

    @Override
    protected void executeJob(JobExecutionContext context, DomainDTO domain) throws JobExecutionException {
        LOG.debug("Start DSS trusted lists refresh job");
        tslValidationJob.refresh();
        LOG.debug("DSS trusted lists refreshed");
    }
}

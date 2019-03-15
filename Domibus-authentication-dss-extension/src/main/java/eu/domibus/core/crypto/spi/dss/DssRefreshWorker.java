package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class DssRefreshWorker extends QuartzJobBean {

    private static final Logger LOG = LoggerFactory.getLogger(DssRefreshWorker.class);

    private TSLValidationJob tslValidationJob;

    @Autowired
    private DssRefreshWorker dssRefreshWorker;

    public DssRefreshWorker() {
    }

    @Override
    protected void executeInternal(org.quartz.JobExecutionContext context) throws JobExecutionException {
        LOG.debug("Start DSS trusted lists refresh job");
        tslValidationJob.refresh();
        LOG.debug("DSS trusted lists refreshed");
    }
}

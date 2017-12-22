package eu.domibus.core.certificate;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@DisallowConcurrentExecution
public class SaveCertificateAndLogRevocationJob extends QuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveCertificateAndLogRevocationJob.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("Checking certificate expiration");
        try {
            certificateService.saveCertificateAndLogRevocation();
        } catch (eu.domibus.api.security.CertificateException ex) {
            LOG.warn("An problem occured while loading keystore:[{}]", ex.getMessage());
        }
    }
}

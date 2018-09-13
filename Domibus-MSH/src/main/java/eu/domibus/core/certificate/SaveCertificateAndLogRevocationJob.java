package eu.domibus.core.certificate;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@DisallowConcurrentExecution
public class SaveCertificateAndLogRevocationJob extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveCertificateAndLogRevocationJob.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain){
        LOG.info("Checking certificate expiration");
        try {
            certificateService.saveCertificateAndLogRevocation(domain);
            certificateService.sendCertificateAlerts();
        } catch (eu.domibus.api.security.CertificateException ex) {
            LOG.warn("An problem occured while loading keystore:[{}]", ex.getMessage(), ex);
        }
    }
}

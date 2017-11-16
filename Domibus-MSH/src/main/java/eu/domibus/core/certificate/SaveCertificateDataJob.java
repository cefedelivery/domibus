package eu.domibus.core.certificate;

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
public class SaveCertificateDataJob extends QuartzJobBean {

    @Autowired
    private CertificateService certificateService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        certificateService.saveCertificateData();
    }
}

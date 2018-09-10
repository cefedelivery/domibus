package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Thomas Dussart, Cosmin Baciu
 * @since 4.0
 */
@Service
public class AlertMethodEmail implements AlertMethod {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertMethodEmail.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Override
    public void sendAlert(Alert alert) {
        final MailModel mailModelForAlert = alertService.getMailModelForAlert(alert);
        LOG.debug("Sending alert by email [{}]", alert);
        mailSender.sendMail(
                mailModelForAlert,
                multiDomainAlertConfigurationService.getCommonConfiguration().getSendFrom(),
                multiDomainAlertConfigurationService.getCommonConfiguration().getSendTo());
    }
}

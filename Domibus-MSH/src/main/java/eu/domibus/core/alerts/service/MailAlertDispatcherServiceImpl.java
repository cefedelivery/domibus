package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static eu.domibus.core.alerts.MailSender.DOMIBUS_ALERT_MAIL_SENDING_ACTIVE;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class MailAlertDispatcherServiceImpl implements AlertDispatcherService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(MailAlertDispatcherServiceImpl.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    @Transactional
    public void dispatch(Alert alert) {
        final boolean mailActive=Boolean.parseBoolean(domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE));
        if(mailActive) {
            final MailModel mailModelForAlert = alertService.getMailModelForAlert(alert);
            try {
                alert.setAlertStatus(AlertStatus.FAILED);
                LOG.debug("Alert:[{}] sending by email...", alert.getEntityId());
                mailSender.sendMail(
                        mailModelForAlert,
                        multiDomainAlertConfigurationService.getCommonConfiguration().getSendFrom(),
                        multiDomainAlertConfigurationService.getCommonConfiguration().getSendTo());
                alert.setAlertStatus(AlertStatus.SUCCESS);
            } finally {
                alertService.handleAlertStatus(alert);
            }
        }
    }

}

package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.alerts.model.common.AlertStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class MailAlertDispatcherServiceImpl implements AlertDispatcherService {

    private final static Logger LOG = LoggerFactory.getLogger(MailAlertDispatcherServiceImpl.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Override
    @Transactional
    public void dispatch(Alert alert) {
        final MailModel mailModelForAlert = alertService.getMailModelForAlert(alert);
        try {
            alert.setAlertStatus(AlertStatus.FAILED);
            mailSender.sendMail(
                    mailModelForAlert,
                    multiDomainAlertConfigurationService.getCommonConfiguration().getSendFrom(),
                    multiDomainAlertConfigurationService.getCommonConfiguration().getSendFrom());
            alert.setAlertStatus(AlertStatus.SUCCESS);
        } finally {
            alertService.handleAlertStatus(alert);
        }
    }

}

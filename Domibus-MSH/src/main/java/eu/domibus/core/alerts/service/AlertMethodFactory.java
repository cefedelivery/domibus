package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.core.alerts.MailSender.DOMIBUS_ALERT_MAIL_SENDING_ACTIVE;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class AlertMethodFactory {

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected AlertMethodEmail alertEmailMethod;

    @Autowired
    protected AlertMethodLog alertEmailLog;

    public AlertMethod getAlertMethod() {
        AlertMethod result = alertEmailLog;
        final boolean mailActive = Boolean.parseBoolean(domibusPropertyProvider.getDomainProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE));
        if (mailActive) {
            result = alertEmailMethod;
        }
        return result;
    }
}

package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



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

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    public AlertMethod getAlertMethod() {
        AlertMethod result = alertEmailLog;
        final String sendEmailActivePropertyName = multiDomainAlertConfigurationService.getSendEmailActivePropertyName();
        final boolean mailActive = Boolean.parseBoolean(domibusPropertyProvider.getOptionalDomainProperty(sendEmailActivePropertyName));
        if (mailActive) {
            result = alertEmailMethod;
        }
        return result;
    }
}

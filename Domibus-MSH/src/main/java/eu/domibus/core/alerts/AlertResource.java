package eu.domibus.core.alerts;

import com.google.common.collect.Lists;
import eu.domibus.core.alerts.dao.AlertCriteria;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.MessageEvent;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.web.rest.ro.ErrorLogResultRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/rest/alerts")
public class AlertResource {

    private final static Logger LOG = LoggerFactory.getLogger(AlertResource.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainExtConverter domibusDomainConverter;


    @RequestMapping(method = RequestMethod.GET)
    public List<AlertRo> findAlerts(@RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    @RequestParam(value = "orderBy", required = false) String column,
                                    @RequestParam(value = "processed", required = false) boolean processed,
                                    @RequestParam(value = "alertType", required = false) String alertType,
                                    @RequestParam(value = "alertId", required = false) String alertId,
                                    @RequestParam(value = "alertLevel", required = false) String alertLevel,
                                    @RequestParam(value = "creationFrom", required = false) String creationFrom,
                                    @RequestParam(value = "creationTo", required = false) String creationTo,
                                    @RequestParam(value = "reportingFrom", required = false) String reportingFrom,
                                    @RequestParam(value = "reportingTo", required = false) String reportingTo,
                                    @RequestParam(value = "parameters", required = false) String[] parameters
                                    ) {
        AlertCriteria alertCriteria=new AlertCriteria();
        final List<AlertRo> convert = domibusDomainConverter.convert(alertService.findAlerts(alertCriteria), AlertRo.class);
        return convert;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/types")
    public List<String> getAlertTypes() {
        final List<AlertType> alertTypes = Lists.newArrayList(AlertType.values());
        return alertTypes.stream().map(Enum::name).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/levels")
    public List<String> getAlertLevels() {
        final List<AlertLevel> alertLevels = Lists.newArrayList(AlertLevel.values());
        return alertLevels.stream().map(Enum::name).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/params")
    public List<String> getAlertParameters(@RequestParam(value = "alertType") String aType) {
        final AlertType alertType = AlertType.valueOf(aType);
        switch (alertType) {
            case MSG_COMMUNICATION_FAILURE:
                final List<MessageEvent> messageEvents = Lists.newArrayList(MessageEvent.values());
                return messageEvents.stream().map(Enum::name).collect(Collectors.toList());
            case CERT_EXPIRED:
                return null;
            case CERT_IMMINENT_EXPIRATION:
                return null;
            case USER_ACCOUNT_DISABLED:
                return null;
            case USER_LOGIN_FAILURE:
                return null;
            default:
                return null;
        }

    }
}

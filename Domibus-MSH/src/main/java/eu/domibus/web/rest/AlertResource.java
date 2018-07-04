package eu.domibus.web.rest;

import com.google.common.collect.Lists;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.AlertRo;
import eu.domibus.core.alerts.dao.AlertCriteria;
import eu.domibus.core.alerts.model.common.*;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping(value = "/rest/alerts")
public class AlertResource {

    private final static Logger LOG = LoggerFactory.getLogger(AlertResource.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    DateUtil dateUtil;


    @RequestMapping(method = RequestMethod.GET)
    public AlertResult findAlerts(@RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "asc", defaultValue = "true") Boolean ask,
                                  @RequestParam(value = "orderBy", required = false) String column,
                                  @RequestParam(value = "processed", required = false) String processed,
                                  @RequestParam(value = "alertType", required = false) String alertType,
                                  @RequestParam(value = "alertId", required = false) Integer alertId,
                                  @RequestParam(value = "alertLevel", required = false) String alertLevel,
                                  @RequestParam(value = "creationFrom", required = false) String creationFrom,
                                  @RequestParam(value = "creationTo", required = false) String creationTo,
                                  @RequestParam(value = "reportingFrom", required = false) String reportingFrom,
                                  @RequestParam(value = "reportingTo", required = false) String reportingTo,
                                  @RequestParam(value = "parameters", required = false) String[] parameters
    ) {
        AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.setPage(page);
        alertCriteria.setPageSize(pageSize);
        alertCriteria.setAsk(ask);
        alertCriteria.setColumn(column);
        alertCriteria.setProcessed(processed);
        alertCriteria.setAlertType(alertType);
        alertCriteria.setAlertID(alertId);
        alertCriteria.setAlertLevel(alertLevel);

         if (StringUtils.isNotEmpty(creationFrom)) {
            alertCriteria.setCreationFrom(dateUtil.fromString(creationFrom));
        }
        if (StringUtils.isNotEmpty(creationTo)) {
            alertCriteria.setCreationTo(dateUtil.fromString(creationTo));
        }
        if (StringUtils.isNotEmpty(reportingFrom)) {
            alertCriteria.setReportingFrom(dateUtil.fromString(reportingFrom));
        }

        if (StringUtils.isNotEmpty(reportingTo)) {
            alertCriteria.setReportingTo(dateUtil.fromString(reportingTo));
        }

        if(StringUtils.isEmpty(alertType)){
             alertType=AlertType.MSG_COMMUNICATION_FAILURE.name();
        }
        if(parameters!=null) {
            final List<String> alertParameters = getAlertParameters(alertType);
            final Map<String, String> parametersMap = IntStream.
                    range(0, parameters.length).
                    mapToObj(i -> new SimpleImmutableEntry<>(alertParameters.get(i), parameters[i])).
                    filter(keyValuePair -> !keyValuePair.getValue().isEmpty()).
                    collect(Collectors.toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue));
            alertCriteria.setParameters(parametersMap);
        }

        final Long aLong = alertService.countAlerts(alertCriteria);
        final List<Alert> alerts = alertService.findAlerts(alertCriteria);
        final List<AlertRo> alertRoList = alerts.stream().map(this::transform).collect(Collectors.toList());
        final AlertResult alertResult = new AlertResult();
        alertResult.setCount(aLong.intValue());
        alertResult.setAlertsEntries(alertRoList);
        return alertResult;
    }

    private AlertRo transform(Alert alert) {
        AlertRo alertRo = new AlertRo();
        alertRo.setProcessed(alert.isProcessed());
        alertRo.setAlertId(alert.getEntityId());
        alertRo.setAlertType(alert.getAlertType().name());
        alertRo.setAlertLevel(alert.getAlertLevel().name());
        alertRo.setCreationDate(alert.getCreationTime());
        alertRo.setReportingDate(alert.getReportingTime());

        final List<String> alertParameterNames = getAlertParameters(alert.getAlertType().name());
        final List<String> alertParameterValues = alertParameterNames.
                stream().
                map(paramName -> alert.getEvents().iterator().next().findProperty(paramName)).
                filter(Optional::isPresent).
                map(Optional::get).
                collect(Collectors.toList());
        alertRo.setParameters(alertParameterValues);
        return alertRo;
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
        AlertType alertType;
        try {
            alertType = AlertType.valueOf(aType);
        } catch (IllegalArgumentException e) {
            LOG.trace("Invalid or empty alert type:[{}] sent from the gui ", aType, e);
            return Lists.newArrayList();
        }
        switch (alertType) {
            case MSG_COMMUNICATION_FAILURE:
                final List<MessageEvent> messageEvents = Lists.newArrayList(MessageEvent.values());
                return messageEvents.stream().map(Enum::name).collect(Collectors.toList());
            case CERT_EXPIRED:
            case CERT_IMMINENT_EXPIRATION:
                final List<CertificateEvent> certificateEvents = Lists.newArrayList(CertificateEvent.values());
                return certificateEvents.stream().map(Enum::name).collect(Collectors.toList());
            case USER_ACCOUNT_DISABLED:
            case USER_LOGIN_FAILURE:
                final List<AuthenticationEvent> authenticationEvents = Lists.newArrayList(AuthenticationEvent.values());
                return authenticationEvents.stream().map(Enum::name).collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unsuported alert type.");
        }

    }
}

package eu.domibus.web.rest;

import com.google.common.collect.Lists;
import eu.domibus.api.csv.CsvException;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.model.common.*;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.web.AlertRo;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.csv.CsvCustomColumns;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping(value = "/rest/alerts")
public class AlertResource {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertResource.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    CsvServiceImpl csvServiceImpl;


    @GetMapping
    public AlertResult findAlerts(@RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "asc", defaultValue = "true") Boolean ask,
                                  @RequestParam(value = "orderBy", required = false) String column,
                                  @RequestParam(value = "processed", required = false) String processed,
                                  @RequestParam(value = "alertType", required = false) String alertType,
                                  @RequestParam(value = "alertStatus", required = false) String alertStatus,
                                  @RequestParam(value = "alertId", required = false) Integer alertId,
                                  @RequestParam(value = "alertLevel", required = false) String alertLevel,
                                  @RequestParam(value = "creationFrom", required = false) String creationFrom,
                                  @RequestParam(value = "creationTo", required = false) String creationTo,
                                  @RequestParam(value = "reportingFrom", required = false) String reportingFrom,
                                  @RequestParam(value = "reportingTo", required = false) String reportingTo,
                                  @RequestParam(value = "parameters", required = false) String[] parameters,
                                  @RequestParam(value = "dynamicFrom", required = false) String dynamicaPropertyFrom,
                                  @RequestParam(value = "dynamicTo", required = false) String dynamicaPropertyTo
    ) {
        AlertCriteria alertCriteria = getAlertCriteria(
                page,
                pageSize,
                ask,
                column,
                processed,
                alertType,
                alertStatus,
                alertId,
                alertLevel,
                creationFrom,
                creationTo,
                reportingFrom,
                reportingTo,
                parameters,
                dynamicaPropertyFrom,
                dynamicaPropertyTo);

        final Long aLong = alertService.countAlerts(alertCriteria);
        final List<Alert> alerts = alertService.findAlerts(alertCriteria);
        final List<AlertRo> alertRoList = alerts.stream().map(this::transform).collect(Collectors.toList());
        final AlertResult alertResult = new AlertResult();
        alertResult.setCount(aLong.intValue());
        alertResult.setAlertsEntries(alertRoList);
        return alertResult;
    }

    @GetMapping(path = "/types")
    public List<String> getAlertTypes() {
        final List<AlertType> alertTypes = Lists.newArrayList(AlertType.values());
        return alertTypes.stream().map(Enum::name).collect(Collectors.toList());
    }

    @GetMapping(path = "/levels")
    public List<String> getAlertLevels() {
        final List<AlertLevel> alertLevels = Lists.newArrayList(AlertLevel.values());
        return alertLevels.stream().map(Enum::name).collect(Collectors.toList());
    }

    @GetMapping(path = "/status")
    public List<String> getAlertStatus() {
        final List<AlertStatus> alertLevels = Lists.newArrayList(AlertStatus.values());
        return alertLevels.stream().filter(alertStatus -> AlertStatus.SEND_ENQUEUED!=alertStatus).map(Enum::name).collect(Collectors.toList());
    }

    @GetMapping(path = "/params")
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

    @PutMapping
    public void processAlerts(@RequestBody List<AlertRo> alertRos) {
        final List<Alert> alerts = alertRos.stream().filter(Objects::nonNull).map(alertRo -> {
            final int entityId = alertRo.getEntityId();
            final boolean processed = alertRo.isProcessed();
            Alert alert = new Alert();
            alert.setEntityId(entityId);
            alert.setProcessed(processed);
            return alert;
        }).collect(Collectors.toList());
        alertService.updateAlertProcessed(alerts);
    }

    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                         @RequestParam(value = "asc", defaultValue = "true") Boolean ask,
                                         @RequestParam(value = "orderBy", required = false) String column,
                                         @RequestParam(value = "processed", required = false) String processed,
                                         @RequestParam(value = "alertType", required = false) String alertType,
                                         @RequestParam(value = "alertStatus", required = false) String alertStatus,
                                         @RequestParam(value = "alertId", required = false) Integer alertId,
                                         @RequestParam(value = "alertLevel", required = false) String alertLevel,
                                         @RequestParam(value = "creationFrom", required = false) String creationFrom,
                                         @RequestParam(value = "creationTo", required = false) String creationTo,
                                         @RequestParam(value = "reportingFrom", required = false) String reportingFrom,
                                         @RequestParam(value = "reportingTo", required = false) String reportingTo,
                                         @RequestParam(value = "parameters", required = false) String[] nonDateDynamicParameters,
                                         @RequestParam(value = "dynamicFrom", required = false) String dynamicaPropertyFrom,
                                         @RequestParam(value = "dynamicTo", required = false) String dynamicaPropertyTo
    ) {
        AlertCriteria alertCriteria = getAlertCriteria(
                page,
                pageSize,
                ask,
                column,
                processed,
                alertType,
                alertStatus,
                alertId,
                alertLevel,
                creationFrom,
                creationTo,
                reportingFrom,
                reportingTo,
                nonDateDynamicParameters,
                dynamicaPropertyFrom,
                dynamicaPropertyTo);

        final List<Alert> alerts = alertService.findAlerts(alertCriteria);
        final List<AlertRo> alertRoList = alerts.stream().map(this::transform).collect(Collectors.toList());
        String resultText;
        try {

            resultText = csvServiceImpl.exportToCSV(alertRoList, AlertRo.class,
                    CsvCustomColumns.ALERT_RESOURCE.getCustomColumns(), new ArrayList<>());

        } catch (CsvException e) {
            LOG.error("Exception caught during export to CSV", e);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + csvServiceImpl.getCsvFilename("alerts"))
                .body(resultText);

    }

    private AlertCriteria getAlertCriteria(int page, int pageSize, Boolean ask, String column, String processed, String alertType, String alertStatus,Integer alertId, String alertLevel, String creationFrom, String creationTo, String reportingFrom, String reportingTo, String[] parameters, String dynamicaPropertyFrom, String dynamicaPropertyTo) {
        AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.setPage(page);
        alertCriteria.setPageSize(pageSize);
        alertCriteria.setAsk(ask);
        alertCriteria.setColumn(column);
        alertCriteria.setProcessed(processed);
        alertCriteria.setAlertType(alertType);
        alertCriteria.setAlertID(alertId);
        alertCriteria.setAlertLevel(alertLevel);
        alertCriteria.setAlertStatus(alertStatus);

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

        if (StringUtils.isEmpty(alertType)) {
            alertType = AlertType.MSG_COMMUNICATION_FAILURE.name();
        }
        if (parameters != null) {
            final List<String> nonDateParameters = getNonDateParameters(alertType);
            final Map<String, String> parametersMap = IntStream.
                    range(0, parameters.length).
                    mapToObj(i -> new SimpleImmutableEntry<>(nonDateParameters.get(i), parameters[i])).
                    filter(keyValuePair -> !keyValuePair.getValue().isEmpty()).
                    collect(Collectors.toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue)); //NOSONAR
            alertCriteria.setParameters(parametersMap);
        }
        final String uniqueDynamicDateParameter = getUniqueDynamicDateParameter(alertType);
        alertCriteria.setUniqueDynamicDateParameter(uniqueDynamicDateParameter);
        if (StringUtils.isNotEmpty(dynamicaPropertyFrom)) {
            alertCriteria.setDynamicaPropertyFrom(dateUtil.fromString(dynamicaPropertyFrom));
        }

        if (StringUtils.isNotEmpty(dynamicaPropertyTo)) {
            alertCriteria.setDynamicaPropertyTo(dateUtil.fromString(dynamicaPropertyTo));
        }

        return alertCriteria;
    }

    private List<String> getNonDateParameters(String alertType) {
        return getAlertParameters(alertType).stream().filter(s -> !(s.endsWith("_TIME") || s.endsWith("_DATE"))).collect(Collectors.toList());
    }

    private String getUniqueDynamicDateParameter(String alertType) {
        final List<String> collect = getAlertParameters(alertType).stream().filter(s -> (s.endsWith("_TIME") || s.endsWith("_DATE"))).collect(Collectors.toList());
        if (collect.size() > 1) {
            throw new IllegalStateException("Only one dynamic date per alert type is supported right now.");
        }
        if (collect.isEmpty()) {
            return null;
        }
        return collect.get(0);

    }

    private AlertRo transform(Alert alert) {
        AlertRo alertRo = new AlertRo();
        alertRo.setProcessed(alert.isProcessed());
        alertRo.setEntityId(alert.getEntityId());
        alertRo.setAlertType(alert.getAlertType().name());
        alertRo.setAlertLevel(alert.getAlertLevel().name());
        alertRo.setCreationTime(alert.getCreationTime());
        alertRo.setReportingTime(alert.getReportingTime());
        alertRo.setAlertStatus(alert.getAlertStatus().name());
        alertRo.setAttempts(alert.getAttempts());
        alertRo.setMaxAttempts(alert.getMaxAttempts());
        alertRo.setReportingTimeFailure(alert.getReportingTimeFailure());
        alertRo.setNextAttempt(alert.getNextAttempt());

        final List<String> alertParameterNames = getAlertParameters(alert.getAlertType().name());
        final List<String> alertParameterValues = alertParameterNames.
                stream().
                map(paramName -> alert.getEvents().iterator().next().findOptionalProperty(paramName)).
                filter(Optional::isPresent).
                map(Optional::get).
                collect(Collectors.toList());
        alertRo.setParameters(alertParameterValues);
        return alertRo;
    }

}

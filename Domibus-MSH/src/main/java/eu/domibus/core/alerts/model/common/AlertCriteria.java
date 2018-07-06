package eu.domibus.core.alerts.model.common;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AlertCriteria {

    private final static Logger LOG = LoggerFactory.getLogger(AlertCriteria.class);

    private int page;

    private int pageSize;

    private Boolean ask;

    private String column;

    private Boolean processed;

    private AlertType alertType;

    private AlertLevel alertLevel;

    private Integer alertID;

    private Date creationFrom;

    private Date creationTo;

    private Date reportingFrom;

    private Date reportingTo;

    private Map<String, String> parameters = new HashMap<>();

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getAsk() {
        return ask;
    }

    public void setAsk(Boolean ask) {
        this.ask = ask;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public Boolean isProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public void setProcessed(String processed) {
        if(StringUtils.isNotEmpty(processed)) {
            this.processed = Boolean.valueOf(processed);
        }
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public void setAlertType(String alertType) {
        if (StringUtils.isNotEmpty(alertType)) {
            this.alertType = AlertType.valueOf(alertType);
        }
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        if (StringUtils.isNotEmpty(alertLevel)) {
            this.alertLevel = AlertLevel.valueOf(alertLevel);
        }
    }

    public Integer getAlertID() {
        return alertID;
    }

    public void setAlertID(Integer alertID) {
        this.alertID = alertID;
    }

    public Date getCreationFrom() {
        return creationFrom;
    }

    public void setCreationFrom(Date creationFrom) {
        this.creationFrom = creationFrom;
    }

    public Date getCreationTo() {
        return creationTo;
    }

    public void setCreationTo(Date creationTo) {
        this.creationTo = creationTo;
    }

    public Date getReportingFrom() {
        return reportingFrom;
    }

    public void setReportingFrom(Date reportingFrom) {
        this.reportingFrom = reportingFrom;
    }

    public Date getReportingTo() {
        return reportingTo;
    }

    public void setReportingTo(Date reportingTo) {
        this.reportingTo = reportingTo;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}

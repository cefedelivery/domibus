package eu.domibus.core.alerts.dao;

import eu.domibus.core.alerts.model.common.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AlertCriteria {

    private final static Logger LOG = LoggerFactory.getLogger(AlertCriteria.class);

    private Boolean processed;

    private AlertType alertType;

    private Integer alertID;

    private Date creationFrom;

    private Date creationTo;

    private Date reportingFrom;

    private Date reportingTo;

    private Map<String,String> parameters=new HashMap<>();

    public Boolean isProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
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

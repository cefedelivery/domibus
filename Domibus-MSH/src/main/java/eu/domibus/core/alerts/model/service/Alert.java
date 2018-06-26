package eu.domibus.core.alerts.model.service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.AlertStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = Alert.class)
public class Alert {

    public static final String DOMIBUS_ALERT_ACTIVE = "domibus.alert.active";

    private final static Logger LOG = LoggerFactory.getLogger(Alert.class);

    private int entityId;

    private boolean processed;

    private Date processedTime;

    private AlertType alertType;

    private Date reportingTime;

    private Integer attempts;

    private Integer maxAttempts;

    private Date reportingTimeFailure;

    private AlertStatus alertStatus;

    private AlertLevel alertLevel;

    private Set<Event> events = new HashSet<>();

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Date getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(Date processedTime) {
        this.processedTime = processedTime;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Date getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(Date reportingTime) {
        this.reportingTime = reportingTime;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Date getReportingTimeFailure() {
        return reportingTimeFailure;
    }

    public void setReportingTimeFailure(Date reportingTimeFailure) {
        this.reportingTimeFailure = reportingTimeFailure;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "entityId=" + entityId +
                ", processed=" + processed +
                ", processedTime=" + processedTime +
                ", alertType=" + alertType +
                ", reportingTime=" + reportingTime +
                ", attempts=" + attempts +
                ", maxAttempts=" + maxAttempts +
                ", reportingTimeFailure=" + reportingTimeFailure +
                ", events=" + events +
                '}';
    }
}

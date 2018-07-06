package eu.domibus.core.alerts.model.persist;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_ALERT")
@NamedQueries({
        @NamedQuery(name = "Alert.findRetry", query = "FROM Alert a where a.alertStatus='RETRY' and a.nextAttempt < CURRENT_TIMESTAMP()"),
        @NamedQuery(name = "Alert.findAlertToClean", query = "FROM Alert a where a.creationTime<:ALERT_LIMIT_DATE")
})
public class Alert extends AbstractBaseEntity{

    @Column(name = "PROCESSED")
    private boolean processed;

    @Column(name = "PROCESSED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processedTime;

    @Column(name = "ALERT_TYPE")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AlertType alertType;

    @Column(name = "REPORTING_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportingTime;

    @Column(name = "CREATION_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationTime;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    @Column(name = "ATTEMPTS_NUMBER")
    private Integer attempts;

    @Column(name = "MAX_ATTEMPTS_NUMBER")
    @NotNull
    private Integer maxAttempts;

    @Column(name = "REPORTING_TIME_FAILURE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportingTimeFailure;

    @Size(min=1)
    @ManyToMany(mappedBy = "alerts",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private Set<Event> events = new HashSet<>();

    @Column(name = "ALERT_STATUS")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AlertStatus alertStatus;

    @Column(name = "ALERT_LEVEL")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AlertLevel alertLevel;

    public void addEvent(Event event){
        events.add(event);
        event.addAlert(this);
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

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getNextAttempt() {
        return nextAttempt;
    }

    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
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
                "processed=" + processed +
                ", processedTime=" + processedTime +
                ", alertType=" + alertType +
                ", reportingTime=" + reportingTime +
                ", nextAttempt=" + nextAttempt +
                ", attempts=" + attempts +
                ", maxAttempts=" + maxAttempts +
                ", reportingTimeFailure=" + reportingTimeFailure +
                ", alertStatus=" + alertStatus +
                ", alertLevel=" + alertLevel +
                '}';
    }
}

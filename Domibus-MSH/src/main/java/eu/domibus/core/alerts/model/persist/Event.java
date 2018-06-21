package eu.domibus.core.alerts.model.persist;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.krb5.internal.crypto.Aes128;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "TB_EVENT")
public class Event extends AbstractBaseEntity {

    private final static Logger LOG = LoggerFactory.getLogger(Event.class);

    @Column(name = "EVENT_TYPE")
    private String eventType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "REPORTING_TIME")
    private Date reportingTime;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "PROPERTY_TYPE")
    @MapKeyEnumerated
    private Map<String, EventPropertyValue> properties = new HashMap<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "TB_EVENT_ALERT",
            joinColumns = {@JoinColumn(name = "EVENT_ID")},
            inverseJoinColumns = {@JoinColumn(name = "ALERT_ID")}
    )
    private Set<Alert> alerts = new HashSet<>();

    public void addAlert(Alert alert) {
        alerts.add(alert);
    }

    public void addProperty(final String key,final  EventPropertyValue eventPropertyValue){
        properties.put(key,eventPropertyValue);
        eventPropertyValue.setEvent(this);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(Date reportingTime) {
        this.reportingTime = reportingTime;
    }

    public Set<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventType='" + eventType + '\'' +
                ", reportingTime=" + reportingTime +
                ", properties=" + properties +
                ", alerts=" + alerts +
                '}';
    }
}

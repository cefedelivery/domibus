package eu.domibus.core.alerts.model.persist;

import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_EVENT")

@NamedQueries({
//        @NamedQuery(name = "Event.findWithTypeAndPropertyValueIn", query = "SELECT e FROM Event e JOIN e.properties p ON key(p) where e.type=:TYPE and p.key=:PROPERTY and ....")
})

public class Event extends AbstractBaseEntity {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(Event.class);

    @Column(name = "EVENT_TYPE")
    @Enumerated(EnumType.STRING)
    @NotNull
    private EventType type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "REPORTING_TIME")
    @NotNull
    private Date reportingTime;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "key")
    @MapKeyEnumerated
    private Map<String, AbstractEventProperty> properties = new HashMap<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "TB_EVENT_ALERT",
            joinColumns = {@JoinColumn(name = "FK_EVENT")},
            inverseJoinColumns = {@JoinColumn(name = "FK_ALERT")}
    )
    private Set<Alert> alerts = new HashSet<>();

    @Column(name = "LAST_ALERT_DATE")
    private LocalDate lastAlertDate;


    public void addAlert(Alert alert) {
        alerts.add(alert);
    }

    public void addProperty(final String key, final AbstractEventProperty abstractProperty) {
        abstractProperty.setKey(key);
        properties.put(key, abstractProperty);
        abstractProperty.setEvent(this);
    }

    public Map<String, AbstractEventProperty> getProperties() {
        return properties;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
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

    public void setProperties(Map<String, AbstractEventProperty> properties) {
        this.properties = properties;
    }

    public LocalDate getLastAlertDate() { return lastAlertDate; }

    public void setLastAlertDate(LocalDate lastAlertDate) {
        this.lastAlertDate = lastAlertDate;
    }


    @Override
    public String toString() {
        return "Event{" +
                "id="+getEntityId()+
                "  type='" + type + '\'' +
                ", reportingTime=" + reportingTime +
                ", properties=" + properties +
                ", alerts=" + alerts +
                '}';
    }

    public void enrichProperties() {
        getProperties().forEach((key, eventProperty) -> {
            eventProperty.setEvent(this);
            LOG.debug("Transferring property with key[{}] value[{}] from jms event to persistent event", key,eventProperty.getValue());
        });
    }
}

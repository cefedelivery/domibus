package eu.domibus.core.alerts.model.persist;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "TB_EVENT_PROPERTY")
public class EventPropertyValue extends AbstractBaseEntity {

    @NotNull
    @Column(name = "PROPERTY_TYPE")
    private String key;

    @NotNull
    @Column(name = "PROPERTY_VALUE")
    private String value;

    @NotNull
    @ManyToOne
    @JoinColumn(name="FK_EVENT")
    private Event event;

    public EventPropertyValue() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "EventPropertyValue{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

package eu.domibus.core.alerts.model.persist;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_EVENT_PROPERTY")
public class EventProperty extends AbstractBaseEntity {

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

    public EventProperty() {
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
        return "EventProperty{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

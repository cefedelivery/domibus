package eu.domibus.core.alerts.model.persist;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class EventPropertyValue {

    @Column(name = "PROPERTY_VALUE")
    private String value;

    @ManyToOne
    private Event event;

    public EventPropertyValue(String value) {
        this.value = value;
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
                "value='" + value + '\'' +
                ", event=" + event +
                '}';
    }
}

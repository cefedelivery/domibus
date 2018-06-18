package eu.domibus.core.alerts.model.persist;

import javax.persistence.Embeddable;

@Embeddable
public class EventPropertyValue {


    private String value;

    public EventPropertyValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

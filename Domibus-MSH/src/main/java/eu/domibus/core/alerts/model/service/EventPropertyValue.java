package eu.domibus.core.alerts.model.service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = EventPropertyValue.class)
public class EventPropertyValue {

    private final static Logger LOG = LoggerFactory.getLogger(EventPropertyValue.class);

    private String value;

    public EventPropertyValue(final String value) {
        this.value = value;
    }

    public EventPropertyValue() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

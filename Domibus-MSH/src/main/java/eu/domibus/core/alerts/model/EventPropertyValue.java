package eu.domibus.core.alerts.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPropertyValue {

    private final static Logger LOG = LoggerFactory.getLogger(EventPropertyValue.class);

    private final String value;

    public EventPropertyValue(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

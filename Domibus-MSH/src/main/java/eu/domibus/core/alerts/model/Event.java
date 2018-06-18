package eu.domibus.core.alerts.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Event {

    private final static Logger LOG = LoggerFactory.getLogger(Event.class);

    private Date reportingTime;

    private Map<String, EventPropertyValue> properties=new HashMap<>();

    public Date getReportingTime() {
        return reportingTime;
    }

    public Map<String, EventPropertyValue> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public void addKeyValue(final String key, final String value) {
        properties.put(key,new EventPropertyValue(value));
    }


}

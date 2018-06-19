package eu.domibus.core.alerts.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Event {

    private final static Logger LOG = LoggerFactory.getLogger(Event.class);

    private int entityId;

    private Date reportingTime;

    private final String eventType;

    private Map<String, EventPropertyValue> properties = new HashMap<>();

    public Event(final String eventType) {
        this.reportingTime = new Date();
        this.eventType = eventType;
    }

    public int getEntityId() {
        return entityId;
    }

    public Date getReportingTime() {
        return reportingTime;
    }

    public Map<String, EventPropertyValue> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Optional<String> getProperty(final String key) {
        final EventPropertyValue eventPropertyValue = properties.get(key);
        if(eventPropertyValue==null){
            throw new IllegalArgumentException("Invalid property key");
        }
        if(eventPropertyValue.getValue()==null){
            return Optional.empty();
        }
        return Optional.of(eventPropertyValue.getValue());

    }

    public void addKeyValue(final String key, final String value) {
        properties.put(key, new EventPropertyValue(value));
    }

}

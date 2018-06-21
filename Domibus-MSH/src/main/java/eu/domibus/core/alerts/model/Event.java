package eu.domibus.core.alerts.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = Event.class)
public class Event {

    private final static Logger LOG = LoggerFactory.getLogger(Event.class);

    private int entityId;

    private Date reportingTime;

    private String type;

    private Map<String, EventPropertyValue> properties = new HashMap<>();

    public Event(final String type) {
        this.reportingTime = new Date();
        this.type = type;
    }

    public Event() {
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public Date getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(Date reportingTime) {
        this.reportingTime = reportingTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, EventPropertyValue> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, EventPropertyValue> properties) {
        this.properties = properties;
    }



    public Optional<String> findProperty(final String key) {
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

    @Override
    public String toString() {
        return "Event{" +
                "entityId=" + entityId +
                ", reportingTime=" + reportingTime +
                ", type='" + type + '\'' +
                ", properties=" + properties +
                '}';
    }
}

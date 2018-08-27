package eu.domibus.core.alerts.model.service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = Event.class)
public class Event {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(Event.class);

    private int entityId;

    private Date reportingTime;

    private EventType type;

    private Map<String, AbstractPropertyValue> properties = new HashMap<>();

    public Event(final EventType type) {
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

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Map<String, AbstractPropertyValue> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, AbstractPropertyValue> properties) {
        this.properties = properties;
    }

    public Optional<String> findStringProperty(final String key) {
        final StringPropertyValue stringPropertyValue = (StringPropertyValue) properties.get(key);
        if(stringPropertyValue ==null){
            LOG.error("No event property with such key as key[{}]",key);
            throw new IllegalArgumentException("Invalid property key");
        }
        if(stringPropertyValue.getValue()==null){
            return Optional.empty();
        }
        return Optional.of(stringPropertyValue.getValue());
    }

    public Optional<String> findOptionalProperty(final String key) {
        final AbstractPropertyValue property = properties.get(key);
        if(property ==null || property.getValue()==null){
            return Optional.empty();
        }
        return Optional.of(property.getValue().toString());
    }

    public void addStringKeyValue(final String key, final String value) {
        properties.put(key, new StringPropertyValue(key,value));
    }

    public void addDateKeyValue(final String key, final Date value) {
        properties.put(key, new DatePropertyValue(key,value));
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

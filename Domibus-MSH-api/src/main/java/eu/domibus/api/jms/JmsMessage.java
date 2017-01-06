package eu.domibus.api.jms;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public class JmsMessage {

    public static final String PROPERTY_ORIGINAL_QUEUE = "originalQueue";

    protected String id;
    protected String type;
    protected String content;
    protected Date timestamp;

    protected Map<String, Object> properties = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public <T> T getProperty(String name) {
        return (T) properties.get(name);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Map<String, Object> getJMSProperties() {
        Map<String, Object> jmsProperties = new HashMap<>();
        for (String key : properties.keySet()) {
            if (key.startsWith("JMS")) {
                jmsProperties.put(key, properties.get(key));
            }
        }
        return jmsProperties;
    }

    public Map<String, Object> getCustomProperties() {
        Map<String, Object> customProperties = new HashMap<>();
        for (String key : properties.keySet()) {
            if (!key.startsWith("JMS")) {
                customProperties.put(key, properties.get(key));
            }
        }
        return customProperties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("content", content)
                .append("timestamp", timestamp)
                .append("properties", properties)
                .toString();
    }
}

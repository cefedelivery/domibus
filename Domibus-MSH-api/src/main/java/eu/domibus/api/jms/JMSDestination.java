package eu.domibus.api.jms;

import java.util.HashMap;
import java.util.Map;

/**
 * // TODO Documentation
 * <p>
 * Created by Cosmin Baciu on 17-Aug-16.
 */
public class JMSDestination {

    protected Map<String, Object> properties = new HashMap<>();

    protected String name;
    protected String fullyQualifiedName;
    protected boolean isInternal;
    protected String type;
    protected long numberOfMessages;

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(long numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    @Override
    public String toString() {
        return fullyQualifiedName;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

}

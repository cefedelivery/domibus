package eu.domibus.jms.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
public class JMSDestinationSPI {
    public final static String QUEUE_TYPE = "Queue";
    public final static String TOPIC_TYPE = "Topic";

    protected Map<String, Object> properties = new HashMap<>();

    protected String name;
    //	protected String jndiName;
//	protected String serverAddress;
//	protected Integer serverPort;
    protected String type;
    //	protected ObjectName objectName;
    protected long numberOfMessages;
    protected long numberOfMessagesPending;

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

    public long getNumberOfMessagesPending() {
        return numberOfMessagesPending;
    }

    public void setNumberOfMessagesPending(Long numberOfMessagesPending) {
        this.numberOfMessagesPending = numberOfMessagesPending;
    }

    public String toString() {
        return name;
    }

}

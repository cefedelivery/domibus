package eu.domibus.api.jms;

import java.util.Date;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 02-Sep-16.
 */
public class JMSMessageBuilder {

    private JmsMessage jmsMessage;

    private JMSMessageBuilder() {
        this.jmsMessage = new JmsMessage();
    }

    public static JMSMessageBuilder create() {
        return new JMSMessageBuilder();
    }

    public JMSMessageBuilder id(String id) {
        jmsMessage.setId(id);
        return this;
    }

    public JMSMessageBuilder content(String content) {
        jmsMessage.setContent(content);
        return this;
    }

    public JMSMessageBuilder timestamp(Date timestamp) {
        jmsMessage.setTimestamp(timestamp);
        return this;
    }

    public JMSMessageBuilder properties(Map<String, Object> properties) {
        jmsMessage.setProperties(properties);
        return this;
    }

    public JMSMessageBuilder property(String name, Object value) {
        jmsMessage.getProperties().put(name, value);
        return this;
    }

    public JmsMessage build() {
        return jmsMessage;
    }
}

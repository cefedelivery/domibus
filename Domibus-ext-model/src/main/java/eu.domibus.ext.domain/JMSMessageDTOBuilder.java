package eu.domibus.ext.domain;

import java.util.Date;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class JMSMessageDTOBuilder {

    private JmsMessageDTO jmsMessage;

    private JMSMessageDTOBuilder() {
        this.jmsMessage = new JmsMessageDTO();
    }

    public static JMSMessageDTOBuilder create() {
        return new JMSMessageDTOBuilder();
    }

    public JMSMessageDTOBuilder id(String id) {
        jmsMessage.setId(id);
        return this;
    }

    public JMSMessageDTOBuilder jmsCorrelationId(String id) {
        jmsMessage.setJmsCorrelationId(id);
        return this;
    }

    public JMSMessageDTOBuilder content(String content) {
        jmsMessage.setContent(content);
        return this;
    }

    public JMSMessageDTOBuilder timestamp(Date timestamp) {
        jmsMessage.setTimestamp(timestamp);
        return this;
    }

    public JMSMessageDTOBuilder properties(Map<String, Object> properties) {
        jmsMessage.setProperties(properties);
        return this;
    }

    public JMSMessageDTOBuilder property(String name, Object value) {
        jmsMessage.getProperties().put(name, value);
        return this;
    }

    public JmsMessageDTO build() {
        return jmsMessage;
    }
}

package eu.domibus.jms.spi.helper;

import eu.domibus.jms.spi.JmsMessageSPI;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
public class JmsMessageCreator implements MessageCreator {

    private final JmsMessageSPI jmsMessageSPI;

    public JmsMessageCreator(JmsMessageSPI message) {
        this.jmsMessageSPI = message;
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
        TextMessage result = session.createTextMessage();

        result.setText(jmsMessageSPI.getContent());
        if (jmsMessageSPI.getType() != null) {
            result.setJMSType(jmsMessageSPI.getType());
        }
        Map<String, Object> customProperties = jmsMessageSPI.getCustomProperties();
        if (!customProperties.isEmpty()) {
            for (String pName : customProperties.keySet()) {
                Object pValue = customProperties.get(pName);
                result.setObjectProperty(pName, pValue);
            }
        }
        return result;
    }
}

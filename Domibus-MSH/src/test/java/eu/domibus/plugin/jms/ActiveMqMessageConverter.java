package eu.domibus.plugin.jms;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Enumeration;

/**
 * Created by martifp on 18/04/2016.
 */
public class ActiveMqMessageConverter implements MessageConverter {

    public Object fromMessage(Message message)
            throws JMSException, MessageConversionException {

        MapMessage mapMessage = (MapMessage) message;
        ActiveMQMapMessage messageObject = new ActiveMQMapMessage();

        Enumeration<String> e = mapMessage.getPropertyNames();
        while (e.hasMoreElements()) {
            String propName = e.nextElement();
            messageObject.setString(propName, "");
            //messageObject.setString("destination", mapMessage.getString("destination") );
            //messageObject.setProperties();
        }
        return messageObject;
    }

    public Message toMessage(Object object, Session session)
            throws JMSException, MessageConversionException {

        ActiveMQMapMessage messageObject = (ActiveMQMapMessage) object;
        MapMessage message = session.createMapMessage();
        Enumeration<String> e = messageObject.getPropertyNames();
        while (e.hasMoreElements()) {
            String propName = e.nextElement();
            messageObject.setString(propName, "");
            //messageObject.setString("destination", mapMessage.getString("destination") );
            //messageObject.setProperties();
        }
        return message;
    }
}

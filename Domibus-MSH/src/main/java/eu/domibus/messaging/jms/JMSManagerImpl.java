package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
@Component
@Transactional
public class JMSManagerImpl implements JMSManager {

    @Autowired
    JMSManagerSPI jmsManagerSPI;

    @Autowired
    JMSDestinationMapper jmsDestinationMapper;

    @Autowired
    JMSMessageMapper jmsMessageMapper;

    @Override
    public Map<String, JMSDestination> getDestinations() {
        Map<String, JMSDestinationSPI> destinations = jmsManagerSPI.getDestinations();
        return jmsDestinationMapper.convert(destinations);
    }

    @Override
    public JmsMessage getMessage(String source, String messageId) {
        JmsMessageSPI jmsMessageSPI = jmsManagerSPI.getMessage(source, messageId);
        return jmsMessageMapper.convert(jmsMessageSPI);
    }

    @Override
    public List<JmsMessage> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selector) {
        List<JmsMessageSPI> messagesSPI = jmsManagerSPI.getMessages(source, jmsType, fromDate, toDate, selector);
        return jmsMessageMapper.convert(messagesSPI);
    }


    @Override
    public boolean sendMessage(JmsMessage message, String connectionFactory, String destination, String destinationType) {
        JmsMessageSPI jmsMessageSPI = jmsMessageMapper.convert(message);
        return jmsManagerSPI.sendMessage(jmsMessageSPI, connectionFactory, destination, destinationType);
    }

    @Override
    public boolean deleteMessages(String source, String[] messageIds) {
        return jmsManagerSPI.deleteMessages(source, messageIds);
    }

    @Override
    public boolean moveMessages(String source, String destination, String[] messageIds) {
        return jmsManagerSPI.moveMessages(source, destination, messageIds);
    }
}

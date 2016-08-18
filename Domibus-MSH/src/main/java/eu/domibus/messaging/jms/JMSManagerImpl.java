package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
@Component
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

package eu.domibus.ext.delegate.services.jms;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.services.JMSExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class JMSServiceServiceDelegate implements JMSExtService {

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    protected DomainExtConverter domainConverter;

    @Override
    public void sendMessageToQueue(JmsMessageDTO message, String destination) {
        final JmsMessage jmsMessage = domainConverter.convert(message, JmsMessage.class);
        jmsManager.sendMessageToQueue(jmsMessage, destination);
    }

    @Override
    public void sendMessageToQueue(JmsMessageDTO message, Queue destination) {
        final JmsMessage jmsMessage = domainConverter.convert(message, JmsMessage.class);
        jmsManager.sendMessageToQueue(jmsMessage, destination);
    }

    @Override
    public void sendMapMessageToQueue(JmsMessageDTO message, String destination) {
        final JmsMessage jmsMessage = domainConverter.convert(message, JmsMessage.class);
        jmsManager.sendMapMessageToQueue(jmsMessage, destination);
    }

    @Override
    public void sendMapMessageToQueue(JmsMessageDTO message, Queue destination) {
        final JmsMessage jmsMessage = domainConverter.convert(message, JmsMessage.class);
        jmsManager.sendMapMessageToQueue(jmsMessage, destination);
    }
}

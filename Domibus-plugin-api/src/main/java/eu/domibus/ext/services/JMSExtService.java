package eu.domibus.ext.services;

import eu.domibus.ext.domain.JmsMessageDTO;

import javax.jms.Queue;

/**
 *
 * @author Cosmin Baciu
 * @since 1.2
 */
public interface JMSExtService {

    void sendMessageToQueue(JmsMessageDTO message, String destination);

    void sendMessageToQueue(JmsMessageDTO message, Queue destination);

    void sendMapMessageToQueue(JmsMessageDTO message, String destination);

    void sendMapMessageToQueue(JmsMessageDTO message, Queue destination);

}

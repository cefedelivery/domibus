package eu.domibus.core.alerts;

import eu.domibus.core.alerts.model.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;

public class MessageEventListener{

    private final static Logger LOG = LoggerFactory.getLogger(MessageEventListener.class);


    @JmsListener(containerFactory = "alertJmsListenerContainerFactory",
            destination = "alertMessageQueue",
            selector = "eventType = 'message'")
    public void onEvent(MessageEvent messageEvent){

    }
}

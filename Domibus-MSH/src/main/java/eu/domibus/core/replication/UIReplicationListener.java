package eu.domibus.core.replication;

import eu.domibus.common.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;


@Component
public class UIReplicationListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationListener.class);

    @Autowired
    protected UIReplicationDataService uiReplicationDataService;

    @JmsListener(destination = "domibus.UI.replication", containerFactory = "internalJmsListenerContainerFactory")
    @Transactional(propagation = Propagation.REQUIRED)
    public void processPullRequest(final MapMessage map) throws JMSException {
        final String jmsType = map.getJMSType();

        if("messageReceived".equalsIgnoreCase(jmsType)) {
            uiReplicationDataService.updateMessageReceived(map.getStringProperty(MessageConstants.MESSAGE_ID));
        } else if("status".equalsIgnoreCase(jmsType)) {
            uiReplicationDataService.updateMessageStatusChange(map.getStringProperty(MessageConstants.MESSAGE_ID), MessageStatus.valueOf(map.getStringProperty("status")));
        }
    }
}

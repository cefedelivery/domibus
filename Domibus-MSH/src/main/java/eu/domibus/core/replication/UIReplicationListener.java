package eu.domibus.core.replication;

import eu.domibus.api.multitenancy.DomainContextProvider;
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

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @JmsListener(destination = "domibus.UI.replication", containerFactory = "internalJmsListenerContainerFactory")
    @Transactional(propagation = Propagation.REQUIRED)
    public void processUIReplication(final MapMessage map) throws JMSException {
        final String messageId = map.getStringProperty(MessageConstants.MESSAGE_ID);
        final String domainCode = map.getStringProperty(MessageConstants.DOMAIN);
        LOG.debug("Sending message ID [{}] for domain [{}]", messageId, domainCode);
        domainContextProvider.setCurrentDomain(domainCode);

        final String jmsType = map.getJMSType();

        if("messageReceived".equalsIgnoreCase(jmsType)) {
            uiReplicationDataService.messageReceived(messageId);
        } else if("messageStatusChange".equalsIgnoreCase(jmsType)) {
            uiReplicationDataService.messageStatusChange(messageId, MessageStatus.valueOf(map.getStringProperty("status")));
        } else if("messageSubmitted".equalsIgnoreCase(jmsType)) {
            uiReplicationDataService.messageSubmitted(messageId);
        }
    }
}

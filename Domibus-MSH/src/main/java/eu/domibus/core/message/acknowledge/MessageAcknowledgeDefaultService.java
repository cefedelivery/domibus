package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.acknowledge.MessageAcknowledgeException;
import eu.domibus.api.message.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.ebms3.UserMessageService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.message.ebms3.UserMessageServiceHelper;
import eu.domibus.api.message.ebms3.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAcknowledgeDefaultService implements MessageAcknowledgeService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgeDefaultService.class);

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Autowired
    UserMessageServiceHelper userMessageServiceHelper;


    @Override
    public MessageAcknowledgement acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws MessageAcknowledgeException {
        final UserMessage userMessage = getUserMessage(messageId);
        final String localAccessPointId = getLocalAccessPointId(userMessage);
        final String finalRecipient = userMessageServiceHelper.getFinalRecipient(userMessage);
        return acknowledgeMessage(userMessage, acknowledgeTimestamp, localAccessPointId, finalRecipient, properties);
    }

    private UserMessage getUserMessage(String messageId) {
        final UserMessage userMessage = userMessageService.getMessage(messageId);
        if (userMessage == null) {
            throw new MessageAcknowledgeException(DomibusCoreErrorCode.DOM_001, "Message with ID [" + messageId + "] does not exist");
        }
        return userMessage;
    }

    @Override
    public MessageAcknowledgement acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp) throws MessageAcknowledgeException {
        return acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);
    }

    @Override
    public MessageAcknowledgement acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws MessageAcknowledgeException {
        final UserMessage userMessage = getUserMessage(messageId);
        final String localAccessPointId = getLocalAccessPointId(userMessage);
        final String finalRecipient = userMessageServiceHelper.getFinalRecipient(userMessage);
        return acknowledgeMessage(userMessage, acknowledgeTimestamp, finalRecipient, localAccessPointId, properties);
    }

    @Override
    public MessageAcknowledgement acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp) throws MessageAcknowledgeException {
        return acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, null);
    }


    protected MessageAcknowledgement acknowledgeMessage(final UserMessage userMessage, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException {
        final String user = authUtils.getAuthenticatedUser();
        MessageAcknowledgementEntity entity = messageAcknowledgeConverter.create(user, userMessage.getMessageInfo().getMessageId(), acknowledgeTimestamp, from, to, properties);
        messageAcknowledgementDao.create(entity);
        return messageAcknowledgeConverter.convert(entity);
    }

    @Override
    public List<MessageAcknowledgement> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        final List<MessageAcknowledgementEntity> entities = messageAcknowledgementDao.findByMessageId(messageId);
        return messageAcknowledgeConverter.convert(entities);
    }

    private String getLocalAccessPointId(UserMessage userMessage) {
        return userMessageServiceHelper.getPartyTo(userMessage);
    }

}

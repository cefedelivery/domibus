package eu.domibus.core.replication;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.UserMessageDefaultServiceHelper;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Service dedicate to replicate
 * data in <code>TB_MESSAGE_UI</> table
 * It first reads existing data and then insert it
 *
 * @author Cosmin Baciu, Catalin Enache
 * @since 4.0
 */
@Service
public class UIReplicationDataService {

    @Autowired
    UIMessageDao uiMessageDao;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    MessagingDao messagingDao;

    @Autowired
    UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;


    public void messageReceived(String messageId) {
        //TODO
        //call uiMessageDao
    }


    public void messageStatusChange(String messageId, MessageStatus newStatus) {

        UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            entity.setMessageStatus(newStatus);
            uiMessageDao.update(entity);
        }
    }

    public void messageSubmitted(String messageId) {

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);

        UIMessageEntity entity = new UIMessageEntity();
        entity.setMessageId(messageId);
        entity.setMessageStatus(userMessageLog.getMessageStatus());
        entity.setNotificationStatus(userMessageLog.getNotificationStatus());
        entity.setMshRole(userMessageLog.getMshRole());
        entity.setMessageType(userMessageLog.getMessageType());
        entity.setDeleted(userMessageLog.getDeleted());
        entity.setReceived(userMessageLog.getReceived());
        entity.setSendAttempts(userMessageLog.getSendAttempts());
        entity.setSendAttemptsMax(userMessageLog.getSendAttemptsMax());
        entity.setNextAttempt(userMessageLog.getNextAttempt());
        entity.setFailed(userMessageLog.getFailed());
        entity.setRestored(userMessageLog.getRestored());
        entity.setConversationId(userMessage.getCollaborationInfo().getConversationId());
        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));


        uiMessageDao.create(entity);
    }

}

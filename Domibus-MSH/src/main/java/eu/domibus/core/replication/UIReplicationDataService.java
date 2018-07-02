package eu.domibus.core.replication;

import com.ctc.wstx.util.StringUtil;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.UserMessageDefaultServiceHelper;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationDataService.class);

    @Autowired
    UIMessageDao uiMessageDao;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    MessagingDao messagingDao;

    @Autowired
    SignalMessageLogDao signalMessageLogDao;


    @Autowired
    UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;


    public void messageReceived(String messageId) {
        saveUIMessageFromUserMessageLog(messageId);
    }

    public void messageSubmitted(String messageId) {

        saveUIMessageFromUserMessageLog(messageId);
    }

    public void signalMessageSubmitted(final String messageId) {

        final SignalMessageLog signalMessageLog = signalMessageLogDao.findByMessageId(messageId);
        final SignalMessage signalMessage = messagingDao.findSignalMessageByMessageId(messageId);

        final Messaging messaging = messagingDao.findMessageByMessageId(signalMessage.getMessageInfo().getRefToMessageId());
        final UserMessage userMessage = messaging.getUserMessage();

        UIMessageEntity entity = new UIMessageEntity();
        entity.setMessageId(messageId);
        entity.setMessageStatus(signalMessageLog.getMessageStatus());
        entity.setNotificationStatus(signalMessageLog.getNotificationStatus());
        entity.setMshRole(signalMessageLog.getMshRole());
        entity.setMessageType(signalMessageLog.getMessageType());
        entity.setDeleted(signalMessageLog.getDeleted());
        entity.setReceived(signalMessageLog.getReceived());
        entity.setSendAttempts(signalMessageLog.getSendAttempts());
        entity.setSendAttemptsMax(signalMessageLog.getSendAttemptsMax());
        entity.setNextAttempt(signalMessageLog.getNextAttempt());
        entity.setFailed(signalMessageLog.getFailed());
        entity.setRestored(signalMessageLog.getRestored());
        entity.setConversationId(StringUtils.EMPTY);
        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));


        uiMessageDao.create(entity);

    }


    public void messageStatusChange(String messageId, MessageStatus newStatus) {

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            entity.setMessageStatus(newStatus);
            entity.setDeleted(userMessageLog.getDeleted());
            entity.setFailed(userMessageLog.getFailed());
            entity.setNextAttempt(userMessageLog.getNextAttempt());

            uiMessageDao.update(entity);
        } else {
            LOG.warn("messageStatusChange failed for messageId={}", messageId);
        }
    }

    protected void saveUIMessageFromUserMessageLog(String messageId) {
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

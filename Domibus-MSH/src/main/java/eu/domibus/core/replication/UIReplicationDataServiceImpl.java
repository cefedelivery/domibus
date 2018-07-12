package eu.domibus.core.replication;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.UserMessageDefaultServiceHelper;
import eu.domibus.ebms3.common.model.MessageType;
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
public class UIReplicationDataServiceImpl implements UIReplicationDataService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationDataServiceImpl.class);

    @Autowired
    private UIMessageDao uiMessageDao;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;

    @Override
    public void messageReceived(String messageId) {
        saveUIMessageFromUserMessageLog(messageId);
        LOG.info("UserMessage with messageId={} replicated", messageId);
    }

    @Override
    public void messageSubmitted(String messageId) {
        saveUIMessageFromUserMessageLog(messageId);
        LOG.info("UserMessage with messageId={} replicated", messageId);
    }

    @Override
    public void messageStatusChange(String messageId, MessageStatus newStatus) {

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            entity.setMessageStatus(newStatus);
            entity.setDeleted(userMessageLog.getDeleted());
            entity.setFailed(userMessageLog.getFailed());
            entity.setRestored(userMessageLog.getRestored());
            entity.setNextAttempt(userMessageLog.getNextAttempt());
            entity.setSendAttemptsMax(userMessageLog.getSendAttemptsMax());

            uiMessageDao.update(entity);
        } else {
            UIReplicationDataServiceImpl.LOG.warn("messageStatusChange failed for messageId={}", messageId);
        }
        LOG.info("{}Message with messageId={} synced",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId);
    }

    @Override
    public void messageChange(String messageId) {

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            entity.setMessageStatus(userMessageLog.getMessageStatus());
            entity.setDeleted(userMessageLog.getDeleted());
            entity.setFailed(userMessageLog.getFailed());
            entity.setRestored(userMessageLog.getRestored());
            entity.setNextAttempt(userMessageLog.getNextAttempt());
            entity.setSendAttemptsMax(userMessageLog.getSendAttemptsMax());

            uiMessageDao.update(entity);
        } else {
            UIReplicationDataServiceImpl.LOG.warn("messageChange failed for messageId={}", messageId);
        }
        LOG.info("{}Message with messageId={} synced",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId);
    }

    @Override
    public void signalMessageSubmitted(final String messageId) {
        saveUIMessageFromSignalMessageLog(messageId);
        LOG.info("SignalMessage with messageId={} replicated", messageId);

    }

    @Override
    public void signalMessageReceived(String messageId) {
        saveUIMessageFromSignalMessageLog(messageId);
        LOG.info("SignalMessage with messageId={} replicated", messageId);
    }

    private void saveUIMessageFromSignalMessageLog(String messageId) {
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
        entity.setConversationId(StringUtils.EMPTY);
        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setRefToMessageId(signalMessage.getMessageInfo().getRefToMessageId());
        entity.setFailed(signalMessageLog.getFailed());
        entity.setRestored(signalMessageLog.getRestored());
        entity.setMessageSubtype(signalMessageLog.getMessageSubtype());

        uiMessageDao.create(entity);
    }


    private void saveUIMessageFromUserMessageLog(String messageId) {
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
        entity.setConversationId(userMessage.getCollaborationInfo().getConversationId());
        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setRefToMessageId(userMessage.getMessageInfo().getRefToMessageId());
        entity.setFailed(userMessageLog.getFailed());
        entity.setRestored(userMessageLog.getRestored());
        entity.setMessageSubtype(userMessageLog.getMessageSubtype());

        uiMessageDao.create(entity);
    }


}

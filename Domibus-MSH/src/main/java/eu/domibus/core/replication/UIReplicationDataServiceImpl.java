package eu.domibus.core.replication;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.UserMessageDefaultServiceHelper;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;


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
    private UIMessageDaoImpl uiMessageDao;

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
    }

    @Override
    public void messageSubmitted(String messageId) {
        saveUIMessageFromUserMessageLog(messageId);
    }

    @Override
    public void messageStatusChange(String messageId, MessageStatus newStatus) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            entity.setMessageStatus(userMessageLog.getMessageStatus());
            entity.setDeleted(userMessageLog.getDeleted());
            entity.setNextAttempt(userMessageLog.getNextAttempt());
            entity.setFailed(userMessageLog.getFailed());

            uiMessageDao.update(entity);
        } else {
            UIReplicationDataServiceImpl.LOG.warn("messageStatusChange failed for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced, status={}",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId, newStatus);
    }

    @Override
    public void messageNotificationStatusChange(String messageId, NotificationStatus newStatus) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            entity.setNotificationStatus(userMessageLog.getNotificationStatus());
            uiMessageDao.update(entity);
        } else {
            UIReplicationDataServiceImpl.LOG.warn("messageNotificationStatusChange failed for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced, notificationStatus={}",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId, newStatus);

    }

    @Override
    public void messageChange(String messageId) {

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            updateUIMessage(userMessageLog, entity);

            uiMessageDao.update(entity);
        } else {
            UIReplicationDataServiceImpl.LOG.warn("messageChange failed for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId);
    }

    @Override
    public void signalMessageSubmitted(final String messageId) {
        saveUIMessageFromSignalMessageLog(messageId);
    }

    public void findAndSyncUIMessages() {
        LOG.info("start counting differences for UIReplication");

        List<UIMessageEntity> uiMessageEntityList =
                uiMessageDao.findUIMessagesNotSynced().
                        stream().
                        map(objects -> convertToUIMessageEntity(objects) ).
                        collect(Collectors.toList());

        LOG.info("Found {} differences between native tables and TB_MESSAGE_UI", uiMessageEntityList.size());

        LOG.info("start to update TB_MESSAGE_UI");
        for (UIMessageEntity uiMessageEntity: uiMessageEntityList) {
            uiMessageDao.saveOrUpdate(uiMessageEntity);
        }
        LOG.info("finish to update TB_MESSAGE_UI");

    }

    @Override
    public void signalMessageReceived(String messageId) {
        saveUIMessageFromSignalMessageLog(messageId);
    }

    /**
     * Replicates {@link SignalMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     */
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
        LOG.debug("SignalMessage with messageId={} replicated", messageId);
    }

    /**
     * Replicates {@link UserMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     */
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
        LOG.debug("UserMessage with messageId={} replicated", messageId);
    }

    /**
     * Updates {@link UIMessageEntity} fields with info from {@link UserMessageLog}
     *
     * @param userMessageLog
     * @param entity
     */
    private void updateUIMessage(UserMessageLog userMessageLog, UIMessageEntity entity) {
        entity.setMessageStatus(userMessageLog.getMessageStatus());
        entity.setNotificationStatus(userMessageLog.getNotificationStatus());
        entity.setDeleted(userMessageLog.getDeleted());
        entity.setFailed(userMessageLog.getFailed());
        entity.setRestored(userMessageLog.getRestored());
        entity.setNextAttempt(userMessageLog.getNextAttempt());
        entity.setSendAttempts(userMessageLog.getSendAttempts());
        entity.setSendAttemptsMax(userMessageLog.getSendAttemptsMax());
    }

    private UIMessageEntity convertToUIMessageEntity(Object[] diffRecord) {
        if (null == diffRecord) {
            return null;
        }

        UIMessageEntity entity = new UIMessageEntity();
        entity.setMessageId((String) diffRecord[0]);
        entity.setMessageStatus(MessageStatus.valueOf((String) diffRecord[1]));
        entity.setNotificationStatus(NotificationStatus.valueOf((String) diffRecord[2]));
        entity.setMshRole(MSHRole.valueOf((String) diffRecord[3]));
        entity.setMessageType(MessageType.valueOf((String) diffRecord[4]));
        entity.setDeleted(diffRecord[5] != null ? (Timestamp) diffRecord[5] : null);
        entity.setReceived(diffRecord[6] != null ? (Timestamp) diffRecord[6] : null);
        entity.setSendAttempts(((BigDecimal) diffRecord[7]).intValueExact());
        entity.setSendAttemptsMax(((BigDecimal) diffRecord[8]).intValueExact());
        entity.setNextAttempt(diffRecord[9] != null ? (Timestamp) diffRecord[9] : null);
        entity.setConversationId((String) diffRecord[10]);
        entity.setFromId((String) diffRecord[11]);
        entity.setToId((String) diffRecord[12]);
        entity.setFromScheme((String) diffRecord[13]);
        entity.setToScheme((String) diffRecord[14]);
        entity.setRefToMessageId(((String) diffRecord[15]));
        entity.setFailed(diffRecord[16] != null ? (Timestamp) diffRecord[16] : null);
        entity.setRestored(diffRecord[17] != null ? (Timestamp) diffRecord[17] : null);
        entity.setMessageSubtype(diffRecord[18] != null ? MessageSubtype.valueOf((String) diffRecord[18]) : null);

        return entity;
    }


}

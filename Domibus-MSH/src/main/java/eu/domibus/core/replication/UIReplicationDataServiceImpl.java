package eu.domibus.core.replication;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.converter.DomainCoreConverter;
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

import java.util.Date;


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

    @Autowired
    private DomainCoreConverter domainConverter;

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void messageReceived(String messageId, long jmsTimestamp) {
        saveUIMessageFromUserMessageLog(messageId, jmsTimestamp);
    }


    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void messageSubmitted(String messageId, long jmsTimestamp) {
        LOG.debug("UserMessage={} submitted", messageId);
        saveUIMessageFromUserMessageLog(messageId, jmsTimestamp);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param messageStatus
     * @param jmsTimestamp
     */
    @Override
    public void messageStatusChange(String messageId, MessageStatus messageStatus, long jmsTimestamp) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null && entity.getLastModified().getTime() <= jmsTimestamp) {
            uiMessageDao.updateMessageStatus(messageId, messageStatus, userMessageLog.getDeleted(),
                    userMessageLog.getNextAttempt(), userMessageLog.getFailed(), new Date(jmsTimestamp));
        } else {
            LOG.debug("messageStatusChange skipped for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced, status={}",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId,
                messageStatus);
    }


    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param notificationStatus
     * @param jmsTimestamp
     */
    @Override
    public void messageNotificationStatusChange(String messageId, NotificationStatus notificationStatus, long jmsTimestamp) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null && entity.getLastModified2().getTime() <= jmsTimestamp) {
            uiMessageDao.updateNotificationStatus(messageId, notificationStatus, new Date(jmsTimestamp));
        } else {
            LOG.debug("messageNotificationStatusChange skipped for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced, notificationStatus={}",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId,
                notificationStatus);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void messageChange(String messageId, long jmsTimestamp) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);
        final Date jmsTime = new Date(jmsTimestamp);

        if (entity != null && entity.getLastModified().getTime() <= jmsTimestamp) {
            uiMessageDao.updateMessage(messageId, userMessageLog.getMessageStatus(),
                    userMessageLog.getDeleted(), userMessageLog.getFailed(), userMessageLog.getRestored(),
                    userMessageLog.getNextAttempt(), userMessageLog.getSendAttempts(), userMessageLog.getSendAttemptsMax(),
                    jmsTime);
        } else {
            LOG.debug("messageChange skipped for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void signalMessageSubmitted(String messageId, long jmsTimestamp) {
        LOG.debug("SignalMessage={} submitted", messageId);
        saveUIMessageFromSignalMessageLog(messageId, jmsTimestamp);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void signalMessageReceived(String messageId, long jmsTimestamp) {
        LOG.debug("SignalMessage={} received", messageId);
        saveUIMessageFromSignalMessageLog(messageId, jmsTimestamp);
    }


    /**
     * Replicates {@link SignalMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void saveUIMessageFromSignalMessageLog(String messageId, final long jmsTimestamp) {
        final SignalMessageLog signalMessageLog = signalMessageLogDao.findByMessageId(messageId);
        final SignalMessage signalMessage = messagingDao.findSignalMessageByMessageId(messageId);

        final Messaging messaging = messagingDao.findMessageByMessageId(signalMessage.getMessageInfo().getRefToMessageId());
        final UserMessage userMessage = messaging.getUserMessage();

        UIMessageEntity entity = domainConverter.convert(signalMessageLog, UIMessageEntity.class);

        entity.setEntityId(0); //dozer copies other value here
        entity.setMessageId(messageId);
        entity.setConversationId(StringUtils.EMPTY);
        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setRefToMessageId(signalMessage.getMessageInfo().getRefToMessageId());
        entity.setLastModified(new Date(jmsTimestamp));
        entity.setLastModified2(entity.getLastModified());

        uiMessageDao.create(entity);
        LOG.debug("SignalMessage with messageId={} replicated", messageId);
    }

    /**
     * Replicates {@link UserMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    protected void saveUIMessageFromUserMessageLog(String messageId, long jmsTimestamp) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);

        //using Dozer
        UIMessageEntity entity = domainConverter.convert(userMessageLog, UIMessageEntity.class);

        entity.setEntityId(0); //dozer
        entity.setMessageId(messageId);
        entity.setConversationId(userMessage.getCollaborationInfo().getConversationId());
        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setRefToMessageId(userMessage.getMessageInfo().getRefToMessageId());
        entity.setLastModified(new Date(jmsTimestamp));
        entity.setLastModified2(entity.getLastModified());

        uiMessageDao.create(entity);
        LOG.debug("UserMessage with messageId={} replicated", messageId);
    }



}

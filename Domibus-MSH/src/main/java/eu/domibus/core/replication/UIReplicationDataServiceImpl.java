package eu.domibus.core.replication;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.UserMessageDefaultServiceHelper;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.OptimisticLockException;
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

    /** max no of records to be synchronized using cron job */
    private static final String MAX_ROWS_KEY = "domibus.ui.replication.sync.cron.max.rows";

    private int maxRowsToSync;

    @Autowired
    private UIMessageDaoImpl uiMessageDao;

    @Autowired
    private UIMessageDiffDao uiMessageDiffDao;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainCoreConverter domainConverter;

    @PostConstruct
    public void init() {
        maxRowsToSync = NumberUtils.toInt(domibusPropertyProvider.getDomainProperty(MAX_ROWS_KEY, "1000"));
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     */
    @Override
    public void messageReceived(String messageId) {
        LOG.debug("UserMessage={} received", messageId);
        saveUIMessageFromUserMessageLog(messageId);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     */
    @Override
    public void messageSubmitted(String messageId) {
        LOG.debug("UserMessage={} submitted", messageId);
        saveUIMessageFromUserMessageLog(messageId);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     */
    @Override
    public void messageStatusChange(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            entity.setMessageStatus(userMessageLog.getMessageStatus());
            entity.setDeleted(userMessageLog.getDeleted());
            entity.setNextAttempt(userMessageLog.getNextAttempt());
            entity.setFailed(userMessageLog.getFailed());

            updateAndFlush(messageId, entity, "messageStatusChange");
        } else {
            LOG.warn("messageStatusChange failed for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced, status={}",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId,
                userMessageLog.getMessageStatus());
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     */
    @Override
    public void messageNotificationStatusChange(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            entity.setNotificationStatus(userMessageLog.getNotificationStatus());

            updateAndFlush(messageId, entity, "messageNotificationStatusChange");
        } else {
            UIReplicationDataServiceImpl.LOG.warn("messageNotificationStatusChange failed for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced, notificationStatus={}",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId,
                userMessageLog.getNotificationStatus());

    }


    /**
     * {@inheritDoc}
     *
     * @param messageId
     */
    @Override
    public void messageChange(String messageId) {

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity != null) {
            updateUIMessage(userMessageLog, entity);

            updateAndFlush(messageId, entity, "messageChange");
        } else {
            UIReplicationDataServiceImpl.LOG.warn("messageChange failed for messageId={}", messageId);
        }
        LOG.debug("{}Message with messageId={} synced",
                MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     */
    @Override
    public void signalMessageSubmitted(final String messageId) {
        LOG.debug("SignalMessage={} submitted", messageId);
        saveUIMessageFromSignalMessageLog(messageId);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     */
    @Override
    public void signalMessageReceived(final String messageId) {
        LOG.debug("SignalMessage={} received", messageId);
        saveUIMessageFromSignalMessageLog(messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findAndSyncUIMessages() {
        LOG.debug("start counting differences for UIReplication");

        int rowsToSyncCount = uiMessageDiffDao.countAll();
        LOG.info("Found {} differences between native tables and TB_MESSAGE_UI", rowsToSyncCount);

        if (rowsToSyncCount > maxRowsToSync) {
            LOG.warn(WarningUtil.warnOutput("There are more than {} rows to sync into TB_MESSAGE_UI table " +
                    "please use the REST resource instead."), maxRowsToSync);
            return;
        }

        List<UIMessageEntity> uiMessageEntityList =
                uiMessageDiffDao.findAll().
                        stream().
                        map(objects -> convertToUIMessageEntity(objects)).
                        collect(Collectors.toList());

        if (!uiMessageEntityList.isEmpty()) {
            LOG.info("start to update TB_MESSAGE_UI");
            try {
                uiMessageEntityList.parallelStream().forEach(uiMessageEntity ->
                        uiMessageDao.saveOrUpdate(uiMessageEntity));
            } catch (OptimisticLockException e) {
                LOG.warn("Optimistic lock exception detected");
            }
            LOG.info("finish to update TB_MESSAGE_UI");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int findAndSyncUIMessages(int limit) {
        LOG.debug("find and sync first {} UIMessages", limit);
        long startTime = System.currentTimeMillis();


        int recordsToSync = uiMessageDiffDao.countAll();

        LOG.debug("{} milliseconds to count the records", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();

        if (recordsToSync == 0) {
            LOG.info("no records to sync");
            return 0;
        }

        List<UIMessageEntity> uiMessageEntityList =
                uiMessageDiffDao.findAll(limit).
                        stream().
                        map(objects -> convertToUIMessageEntity(objects)).
                        collect(Collectors.toList());

        LOG.debug("{} milliseconds to fetch the records", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();

        if (!uiMessageEntityList.isEmpty()) {
            LOG.debug("start to update TB_MESSAGE_UI");
            try {
                uiMessageEntityList.parallelStream().forEach(uiMessageEntity ->
                        uiMessageDao.saveOrUpdate(uiMessageEntity));
            } catch (OptimisticLockException e) {
                LOG.warn("Optimistic lock exception detected");
            }
            LOG.debug("finish to update TB_MESSAGE_UI after {} milliseconds", System.currentTimeMillis() - startTime);
        }
        return uiMessageEntityList.size();
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public int countSyncUIMessages() {
        LOG.debug("start to count UIMessages to be synced");
        long startTime = System.currentTimeMillis();

        int recordsToSync = uiMessageDiffDao.countAll();

        LOG.debug("{} milliseconds to count the records", System.currentTimeMillis() - startTime);

        return recordsToSync;
    }

    /**
     * Update (merge) a JPA entity and force the flush in order to catch right away the {@link OptimisticLockException}
     *
     * @param messageId
     * @param entity
     * @param operationName
     */
    void updateAndFlush(String messageId, UIMessageEntity entity, String operationName) {
        try {
            uiMessageDao.update(entity);
            uiMessageDao.flush();
        } catch (StaleObjectStateException | OptimisticLockException e) {
            LOG.debug("Optimistic lock detected for {} on messageId={}", operationName, messageId);
        }
    }

    /**
     * Replicates {@link SignalMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     */
    void saveUIMessageFromSignalMessageLog(String messageId) {
        final SignalMessageLog signalMessageLog = signalMessageLogDao.findByMessageId(messageId);
        final SignalMessage signalMessage = messagingDao.findSignalMessageByMessageId(messageId);

        final Messaging messaging = messagingDao.findMessageByMessageId(signalMessage.getMessageInfo().getRefToMessageId());
        final UserMessage userMessage = messaging.getUserMessage();

        UIMessageEntity entity = domainConverter.convert(signalMessageLog, UIMessageEntity.class);

        entity.setMessageId(messageId);
        entity.setConversationId(StringUtils.EMPTY);
        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setRefToMessageId(signalMessage.getMessageInfo().getRefToMessageId());

        uiMessageDao.create(entity);
        LOG.debug("SignalMessage with messageId={} replicated", messageId);
    }

    /**
     * Replicates {@link UserMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     */
    void saveUIMessageFromUserMessageLog(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);

        //using Dozer
        UIMessageEntity entity = domainConverter.convert(userMessageLog, UIMessageEntity.class);

        entity.setMessageId(messageId);
        entity.setConversationId(userMessage.getCollaborationInfo().getConversationId());
        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setRefToMessageId(userMessage.getMessageInfo().getRefToMessageId());

        uiMessageDao.create(entity);
        LOG.debug("UserMessage with messageId={} replicated", messageId);
    }

    /**
     * Updates {@link UIMessageEntity} fields with info from {@link UserMessageLog}
     *
     * @param userMessageLog
     * @param entity
     */
    void updateUIMessage(UserMessageLog userMessageLog, UIMessageEntity entity) {
        entity.setMessageStatus(userMessageLog.getMessageStatus());
        entity.setNotificationStatus(userMessageLog.getNotificationStatus());
        entity.setDeleted(userMessageLog.getDeleted());
        entity.setFailed(userMessageLog.getFailed());
        entity.setRestored(userMessageLog.getRestored());
        entity.setNextAttempt(userMessageLog.getNextAttempt());
        entity.setSendAttempts(userMessageLog.getSendAttempts());
        entity.setSendAttemptsMax(userMessageLog.getSendAttemptsMax());
    }

    /**
     * Converts one record of the diff query to {@link UIMessageEntity}
     *
     * @param diffEntity
     * @return
     */
    UIMessageEntity convertToUIMessageEntity(UIMessageDiffEntity diffEntity) {
        if (null == diffEntity) {
            return null;
        }

        return domainConverter.convert(diffEntity, UIMessageEntity.class);
    }


}

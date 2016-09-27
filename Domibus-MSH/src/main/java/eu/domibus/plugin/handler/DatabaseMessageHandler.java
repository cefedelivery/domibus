/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.plugin.handler;

/**
 * @author Christian Koch, Stefan Mueller
 * @Since 3.0
 */

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.*;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.exception.MessagingExceptionFactory;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLogBuilder;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.persistence.NoResultException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseMessageHandler implements MessageSubmitter<Submission>, MessageRetriever<Submission> {

    private static final Log LOG = LogFactory.getLog(DatabaseMessageHandler.class);

    private final ObjectFactory objectFactory = new ObjectFactory();

    private final ObjectFactory ebMS3Of = new ObjectFactory();

    @Autowired
    JMSManager jmsManager;

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue sendMessageQueue;

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private SubmissionAS4Transformer transformer;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private PayloadProfileValidator payloadProfileValidator;

    @Autowired
    private PropertyProfileValidator propertyProfileValidator;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        DatabaseMessageHandler.LOG.info("looking for message with id: " + messageId);
        final MessageLog userMessageLog;
        final UserMessage userMessage;
        try {
            userMessage = this.messagingDao.findUserMessageByMessageId(messageId);
            userMessageLog = this.userMessageLogDao.findByMessageId(messageId, MSHRole.RECEIVING);
            if (userMessageLog == null) {
                throw new MessageNotFoundException("Message with id [" + messageId + "] was not found");
            }
        } catch (final NoResultException nrEx) {
            DatabaseMessageHandler.LOG.debug("Message with id [" + messageId + "] was not found", nrEx);
            throw new MessageNotFoundException("Message with id [" + messageId + "] was not found");
        }
        // Deleting the message and signal message if the retention download is zero
        if (0 == pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpc())) {
            messagingDao.delete(messageId);
            List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(messageId);
            if (!signalMessages.isEmpty()) {
                for (SignalMessage signalMessage : signalMessages) {
                    signalMessageDao.clear(signalMessage);
                }
            }
        }
        // Updates the User Message log
        userMessageLog.setDeleted(new Date());
        userMessageLogDao.update(userMessageLog);
        // Updates the Signal Message log
        List<String> signalMessageIds = signalMessageDao.findSignalMessageIdsByRefMessageId(messageId);
        if (!signalMessageIds.isEmpty()) {
            for (String signalMessageId : signalMessageIds) {
                signalMessageLogDao.setMessageStatus(signalMessageId, MessageStatus.DELETED);
            }
        }
        return transformer.transformFromMessaging(userMessage);
    }

    @Override
    public MessageStatus getMessageStatus(final String messageId) {
        return this.userMessageLogDao.getMessageStatus(messageId);
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
        return this.errorLogDao.getErrorsForMessage(messageId);
    }


    @Override
    @Transactional
    public String submit(final Submission messageData, final String backendName) throws MessagingProcessingException {
        try {
            final UserMessage userMessage = this.transformer.transformFromSubmission(messageData);
            final MessageInfo messageInfo = userMessage.getMessageInfo();
            if (messageInfo == null) {
                userMessage.setMessageInfo(this.objectFactory.createMessageInfo());
            }
            String messageId = userMessage.getMessageInfo().getMessageId();
            if (messageId == null || userMessage.getMessageInfo().getMessageId().trim().isEmpty()) {
                messageId = messageIdGenerator.generateMessageId();
                userMessage.getMessageInfo().setMessageId(messageId);
            } else if (messageId.length() > 255) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "MessageId value is too long (over 255 characters)", null, null);
            }
            String refToMessageId = userMessage.getMessageInfo().getRefToMessageId();
            if (refToMessageId != null && refToMessageId.length() > 255) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "RefToMessageId value is too long (over 255 characters)", refToMessageId, null);
            }
            // handle if the messageId is unique. This should only fail if the ID is set from the outside
            if (!MessageStatus.NOT_FOUND.equals(userMessageLogDao.getMessageStatus(messageId))) {
                throw new DuplicateMessageException("Message with id [" + messageId + "] already exists. Message identifiers must be unique");
            }

            final String pmodeKey;
            final Messaging message = this.ebMS3Of.createMessaging();
            message.setUserMessage(userMessage);
            try {
                pmodeKey = this.pModeProvider.findPModeKeyForUserMessage(userMessage);
            } catch (IllegalStateException e) { //if no pmodes are configured
                LOG.debug(e);
                throw new PModeMismatchException("PMode could not be found. Are PModes configured in the database?");
            }

            final Party from = pModeProvider.getSenderParty(pmodeKey);
            final Party to = pModeProvider.getReceiverParty(pmodeKey);
            // Verifies that the initiator and responder party are not the same.
            if (from.getName().equals(to.getName())) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "The initiator party's name is the same as the responder party's one[" + from.getName() + "]", null, null);
            }
            // Verifies that the message is not for the current gateway.
            Configuration config = pModeProvider.getConfigurationDAO().read();
            if (config.getParty().equals(to)) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "It is forbidden to submit a message to the sending access point[" + to.getName() + "]", null, null);
            }

            final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pmodeKey);
            final Map<Party, Mpc> mpcMap = legConfiguration.getPartyMpcMap();
            String mpc = Mpc.DEFAULT_MPC;
            if (legConfiguration.getDefaultMpc() != null) {
                mpc = legConfiguration.getDefaultMpc().getQualifiedName();
            }
            if (mpcMap != null && mpcMap.containsKey(to)) {
                mpc = mpcMap.get(to).getQualifiedName();
            }
            userMessage.setMpc(mpc);
            this.payloadProfileValidator.validate(message, pmodeKey);
            this.propertyProfileValidator.validate(message, pmodeKey);
            int sendAttemptsMax = 1;

            if (legConfiguration.getReceptionAwareness() != null) {
                sendAttemptsMax = legConfiguration.getReceptionAwareness().getRetryCount();
            }

            try {
                final boolean compressed = this.compressionService.handleCompression(userMessage, legConfiguration);
                LOG.debug("Compression for message with id: " + userMessage.getMessageInfo().getMessageId() + " applied: " + compressed);
            } catch (final EbMS3Exception e) {
                this.errorLogDao.create(new ErrorLogEntry(e));
                throw e;
            }

            // We do not create MessageIds for SignalMessages, as those should never be submitted via the backend
            messagingDao.create(message);
            // TODO Should we store the user message log before it is dispatched to the queue ?
            // Sends message to the proper queue
            jmsManager.sendMessageToQueue(new DispatchMessageCreator(messageId, to.getEndpoint()).createMessage(), sendMessageQueue);
            // Builds the user message log
            UserMessageLogBuilder umlBuilder = UserMessageLogBuilder.create()
                    .setMessageId(userMessage.getMessageInfo().getMessageId())
                    .setMessageStatus(MessageStatus.SEND_ENQUEUED)
                    .setMshRole(MSHRole.SENDING)
                    .setNotificationStatus(legConfiguration.getErrorHandling().isBusinessErrorNotifyProducer() ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED)
                    .setMpc(message.getUserMessage().getMpc())
                    .setSendAttemptsMax(sendAttemptsMax)
                    .setBackendName(backendName)
                    .setEndpoint(to.getEndpoint());

            userMessageLogDao.create(umlBuilder.build());

            return userMessage.getMessageInfo().getMessageId();

        } catch (final EbMS3Exception e) {
            LOG.error("Error submitting to backendName :" + backendName, e);
            throw MessagingExceptionFactory.transform(e);
        }
    }


}

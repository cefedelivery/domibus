package eu.domibus.ebms3.sender;

import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;


/**
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class MessageSenderService  {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSenderService.class);

    private static final Set<MessageStatus> ALLOWED_STATUSES_FOR_SENDING = EnumSet.of(MessageStatus.SEND_ENQUEUED, MessageStatus.WAITING_FOR_RETRY);
    private static final int MAX_RETRY_COUNT = 3;

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    MessageSenderFactory messageSenderFactory;


    public void sendUserMessage(final String messageId, int retryCount) {
        final MessageStatus messageStatus = userMessageLogDao.getMessageStatus(messageId);

        if (MessageStatus.NOT_FOUND == messageStatus) {
            if (retryCount < MAX_RETRY_COUNT) {
                userMessageService.scheduleSending(messageId, retryCount + 1);
                LOG.warn("MessageStatus NOT_FOUND, retry count is [{}] -> reschedule sending", retryCount);
                return;
            }
            LOG.warn("Message [{}] has a status [{}] for [{}] times and will not be sent", messageId, MessageStatus.NOT_FOUND, retryCount);
            return;
        }

        if (!ALLOWED_STATUSES_FOR_SENDING.contains(messageStatus)) {
            LOG.warn("Message [{}] has a status [{}] which is not allowed for sending. Only the statuses [{}] are allowed", messageId, messageStatus, ALLOWED_STATUSES_FOR_SENDING);
            return;
        }

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        final MessageSender messageSender = messageSenderFactory.getMessageSender(userMessage);
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SEND_INITIATION);
        messageSender.sendMessage(userMessage);
    }

}

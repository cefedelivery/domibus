package eu.domibus.web.rest;

import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/messagelog")
public class MessageLogResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageLogResource.class);

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    DateUtil dateUtil;

    @RequestMapping(method = RequestMethod.GET)
    public MessageLogResultRO getMessageLog(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "orderby", required = false) String column,
            @RequestParam(value = "asc", defaultValue = "true") boolean asc,
            @RequestParam(value = "messageId", required = false) String messageId,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "mshRole", required = false) MSHRole mshRole,
            @RequestParam(value = "messageType", defaultValue = "SIGNAL_MESSAGE") MessageType messageType,
            @RequestParam(value = "messageStatus", required = false) MessageStatus messageStatus,
            @RequestParam(value = "notificationStatus", required = false) NotificationStatus notificationStatus,
            @RequestParam(value = "fromPartyId", required = false) String fromPartyId,
            @RequestParam(value = "endpoint", required = false) String endpoint,
            @RequestParam(value = "refToMessageId", required = false) String refToMessageId,
            @RequestParam(value = "originalSender", required = false) String originalSender,
            @RequestParam(value = "finalReceiver", required = false) String finalReceiver,
            @RequestParam(value = "receivedFrom", required = false) String receivedFrom,
            @RequestParam(value = "receivedTo", required = false) String receivedTo) {

        LOGGER.debug("Getting message log");

        MessageLogResultRO result = new MessageLogResultRO();

        HashMap<String, Object> filters = new HashMap<>();
        filters.put("messageId", messageId);
        filters.put("conversationId", conversationId);
        filters.put("mshRole", mshRole);
        filters.put("messageType", messageType);
        filters.put("messageStatus", messageStatus);
        filters.put("notificationStatus", notificationStatus);
        filters.put("fromPartyId", fromPartyId);
        filters.put("endpoint", endpoint);
        filters.put("refToMessageId", refToMessageId);
        filters.put("originalSender", originalSender);
        filters.put("finalReceiver", finalReceiver);
        filters.put("receivedFrom", dateUtil.fromString(receivedFrom));
        filters.put("receivedTo", dateUtil.fromString(receivedTo));

        result.setFilter(filters);
        LOGGER.debug("using filters [{}]", filters);

        final List<? extends MessageLog> messageLogEntries;
        if(messageType != null )  {
            switch(messageType) {
                case USER_MESSAGE:
                    long entriesUser = userMessageLogDao.countMessages(filters);
                    LOGGER.debug("count User Messages [{}]", entriesUser);
                    result.setCount(Long.valueOf(entriesUser).intValue());
                    messageLogEntries = userMessageLogDao.findPaged(pageSize * page, pageSize, column, asc, filters);
                    result.setMessageLogEntries(convert(messageLogEntries));
                    //List<Object> resultList = userMessageLogDao.findAllInfoPaged(pageSize * page, pageSize, column, asc, filters);
                    //result.setMessageLogEntries(convertObjects(resultList));

                    break;
                case SIGNAL_MESSAGE:
                    long entriesSignal = signalMessageLogDao.countMessages(filters);
                    LOGGER.debug("count Signal Messages [{}]", entriesSignal);
                    result.setCount(Long.valueOf(entriesSignal).intValue());
                    messageLogEntries = signalMessageLogDao.findPaged(pageSize * page, pageSize, column, asc, filters);
                    result.setMessageLogEntries(convert(messageLogEntries));
                    break;
            }
        }

        result.setMshRoles(MSHRole.values());
        result.setMsgTypes(MessageType.values());
        result.setMsgStatus(MessageStatus.values());
        result.setNotifStatus(NotificationStatus.values());
        result.setPage(page);
        result.setPageSize(pageSize);

        return result;
    }

    /*protected List<MessageLogRO> convertObjects(List<Object> objects) {
        List<MessageLogRO> result = new ArrayList<>();
        for(Object object : objects) {
            final MessageLogRO messageLogRO = convertObject(object);
            if(messageLogRO != null) {
                result.add(messageLogRO);
            }
        }
        return result;
    }

    private MessageLogRO convertObject(Object object) {
        if(object == null) {
            return null;
        }

        MessageLogRO result = new MessageLogRO();
        result.setMessageId("1");
        result.setConversationId("conversation1");
        result.setFromPartyId("fromPartyId");
        result.setEndpoint("endpoint");
        result.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        result.setNotificationStatus(NotificationStatus.NOT_REQUIRED);
        result.setMshRole(MSHRole.RECEIVING);
        result.setMessageType(MessageType.USER_MESSAGE);
        result.setDeleted(new Date());
        result.setReceived(new Date());
        result.setSendAttempts(4);
        result.setSendAttemptsMax(4);
        result.setNextAttempt(new Date());
        result.setOriginalSender("originalSender");
        result.setFinalRecipient("finalRecipient");
        result.setRefToMessageId("message1");
        return result;
    }*/

    protected List<MessageLogRO> convert(List<? extends MessageLog> messageLogEntries) {
        List<MessageLogRO> result = new ArrayList<>();
        for(MessageLog messageLogEntry : messageLogEntries) {
            final MessageLogRO messageLogRO = convert(messageLogEntry);
            if(messageLogRO != null) {
                result.add(messageLogRO);
            }
        }
        return result;
    }

    private MessageLogRO convert(MessageLog messageLogEntry) {
        if(messageLogEntry == null) {
            return null;
        }

        MessageLogRO result = new MessageLogRO();
        result.setMessageId(messageLogEntry.getMessageId());
        //result.setConversationId("conversation1");
        //result.setFromPartyId("fromPartyId1");
        result.setEndpoint(messageLogEntry.getEndpoint());
        result.setMessageStatus(messageLogEntry.getMessageStatus());
        result.setNotificationStatus(messageLogEntry.getNotificationStatus());
        result.setMshRole(messageLogEntry.getMshRole());
        result.setMessageType(messageLogEntry.getMessageType());
        result.setDeleted(messageLogEntry.getDeleted());
        result.setReceived(messageLogEntry.getReceived());
        result.setSendAttempts(messageLogEntry.getSendAttempts());
        result.setSendAttemptsMax(messageLogEntry.getSendAttemptsMax());
        result.setNextAttempt(messageLogEntry.getNextAttempt());
        return result;
    }
}

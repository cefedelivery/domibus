package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessagesLogService;
import eu.domibus.core.csv.CsvCustomColumns;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.replication.UIMessageService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Tiago Miguel, Catalin Enache
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/messagelog")
public class MessageLogResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageLogResource.class);

    private static final String RECEIVED_FROM_STR = "receivedFrom";
    private static final String RECEIVED_TO_STR = "receivedTo";

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private CsvServiceImpl csvServiceImpl;

    @Autowired
    private UIMessageService uiMessageService;

    @Autowired
    private MessagesLogService messagesLogService;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    Date defaultFrom;

    Date defaultTo;

    @PostConstruct
    public void init() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        try {
            defaultFrom = ft.parse("1970-01-01 23:59:00");
            defaultTo = ft.parse("2977-10-25 23:59:00");
        } catch (ParseException e) {
            LOG.error("Impossible to initiate default dates");
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public MessageLogResultRO getMessageLog(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,

            @RequestParam(value = "orderBy", required = false) String column,
            @RequestParam(value = "asc", defaultValue = "true") boolean asc,

            @RequestParam(value = "messageId", required = false) String messageId,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "mshRole", required = false) MSHRole mshRole,
            @RequestParam(value = "messageType", defaultValue = "USER_MESSAGE") MessageType messageType,
            @RequestParam(value = "messageStatus", required = false) MessageStatus messageStatus,
            @RequestParam(value = "notificationStatus", required = false) NotificationStatus notificationStatus,
            @RequestParam(value = "fromPartyId", required = false) String fromPartyId,
            @RequestParam(value = "toPartyId", required = false) String toPartyId,
            @RequestParam(value = "refToMessageId", required = false) String refToMessageId,
            @RequestParam(value = "originalSender", required = false) String originalSender,
            @RequestParam(value = "finalRecipient", required = false) String finalRecipient,
            @RequestParam(value = RECEIVED_FROM_STR, required = false) String receivedFrom,
            @RequestParam(value = RECEIVED_TO_STR, required = false) String receivedTo,
            @RequestParam(value = "messageSubtype", required = false) MessageSubtype messageSubtype) {

        LOG.debug("Getting message log");

        //creating the filters
        HashMap<String, Object> filters = createFilterMap(messageId, conversationId, mshRole, messageStatus, notificationStatus,
                fromPartyId, toPartyId, refToMessageId, originalSender, finalRecipient, messageSubtype);

        //we just set default values for received column
        // in order to improve pagination on large amount of data
        Date from = dateUtil.fromString(receivedFrom);
        if (from == null) {
            from = defaultFrom;
        }
        Date to = dateUtil.fromString(receivedTo);
        if (to == null) {
            to = defaultTo;
        }
        filters.put(RECEIVED_FROM_STR, from);
        filters.put(RECEIVED_TO_STR, to);
        filters.put("messageType", messageType);

        LOG.debug("using filters [{}]", filters);

        MessageLogResultRO result;
        if (uiReplicationSignalService.isReplicationEnabled()) {
            /** use TB_MESSAGE_UI table instead */
            result = uiMessageService.countAndFindPaged(pageSize * page, pageSize, column, asc, filters);
        } else {
            //old, fashioned way
            result = messagesLogService.countAndFindPaged(messageType, pageSize * page, pageSize, column, asc, filters);
        }

        if (defaultFrom.equals(from)) {
            filters.remove(RECEIVED_FROM_STR);
        }
        if (defaultTo.equals(to)) {
            filters.remove(RECEIVED_TO_STR);
        }
        result.setFilter(filters);
        result.setMshRoles(MSHRole.values());
        result.setMsgTypes(MessageType.values());
        result.setMsgStatus(MessageStatus.values());
        result.setNotifStatus(NotificationStatus.values());
        result.setPage(page);
        result.setPageSize(pageSize);

        return result;
    }

    /**
     * This method returns a CSV file with the contents of Messages table
     *
     * @param orderByColumn      the column to sort rows by
     * @param asc                true if the sort direction is ascending
     * @param messageId          the message id
     * @param conversationId     the conversation id
     * @param mshRole            the MSH role
     * @param messageType        the message type (SIGNAL_MESSAGE or USER_MESSAGE)
     * @param messageStatus      the message status
     * @param notificationStatus the notification status
     * @param fromPartyId        the sender party id
     * @param toPartyId          the recipient party id
     * @param refToMessageId     the related message id
     * @param originalSender     the original sender
     * @param finalRecipient     the final recipient
     * @param receivedFrom       received after this date
     * @param receivedTo         received before this date
     * @param messageSubtype     the message subtype
     * @return CSV file with the contents of Messages table
     */
    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv(
            @RequestParam(value = "orderBy", required = false) String orderByColumn,
            @RequestParam(value = "asc", defaultValue = "true") boolean asc,

            @RequestParam(value = "messageId", required = false) String messageId,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "mshRole", required = false) MSHRole mshRole,
            @RequestParam(value = "messageType", defaultValue = "USER_MESSAGE") MessageType messageType,
            @RequestParam(value = "messageStatus", required = false) MessageStatus messageStatus,
            @RequestParam(value = "notificationStatus", required = false) NotificationStatus notificationStatus,
            @RequestParam(value = "fromPartyId", required = false) String fromPartyId,
            @RequestParam(value = "toPartyId", required = false) String toPartyId,
            @RequestParam(value = "refToMessageId", required = false) String refToMessageId,
            @RequestParam(value = "originalSender", required = false) String originalSender,
            @RequestParam(value = "finalRecipient", required = false) String finalRecipient,
            @RequestParam(value = RECEIVED_FROM_STR, required = false) String receivedFrom,
            @RequestParam(value = RECEIVED_TO_STR, required = false) String receivedTo,
            @RequestParam(value = "messageSubtype", required = false) MessageSubtype messageSubtype) {

        HashMap<String, Object> filters = createFilterMap(messageId, conversationId, mshRole, messageStatus, notificationStatus,
                fromPartyId, toPartyId, refToMessageId, originalSender, finalRecipient, messageSubtype);

        filters.put(RECEIVED_FROM_STR, dateUtil.fromString(receivedFrom));
        filters.put(RECEIVED_TO_STR, dateUtil.fromString(receivedTo));
        filters.put("messageType", messageType);

        int maxNumberRowsToExport = csvServiceImpl.getMaxNumberRowsToExport();

        List<MessageLogInfo> resultList;
        if (uiReplicationSignalService.isReplicationEnabled()) {
            /** use TB_MESSAGE_UI table instead */
            resultList = uiMessageService.findPaged(0, maxNumberRowsToExport, orderByColumn, asc, filters);
        } else {
            resultList = messagesLogService.findAllInfoCSV(messageType, maxNumberRowsToExport, orderByColumn, asc, filters);
        }

        String resultText;
        try {
            resultText = csvServiceImpl.exportToCSV(resultList, MessageLogInfo.class,
                    CsvCustomColumns.MESSAGE_RESOURCE.getCustomColumns(), new ArrayList<>());
        } catch (CsvException e) {
            LOG.error("Exception caught during export to CSV", e);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + csvServiceImpl.getCsvFilename("messages"))
                .body(resultText);
    }

    @RequestMapping(value = "test/outgoing/latest", method = RequestMethod.GET)
    public ResponseEntity<TestServiceMessageInfoRO> getLastTestSent(@RequestParam(value = "partyId") String partyId) {
        LOG.debug("Getting last sent test message for partyId='{}'", partyId);

        String userMessageId = userMessageLogDao.findLastUserTestMessageId(partyId);
        if(StringUtils.isBlank(userMessageId)) {
            LOG.debug("Could not find last user message id for party [{}]", partyId);
            return ResponseEntity.noContent().build();
        }

        UserMessageLog userMessageLog = null;
        //TODO create a UserMessageLog object independent of Hibernate annotations in the domibus-api and use the UserMessageLogService instead
        try {
            userMessageLog = userMessageLogDao.findByMessageId(userMessageId);
        } catch (NoResultException ex){
            LOG.trace("No UserMessageLog found for message with id [{}]", userMessageId);
        }

        if (userMessageLog != null) {
            TestServiceMessageInfoRO testServiceMessageInfoRO = new TestServiceMessageInfoRO();
            testServiceMessageInfoRO.setMessageId(userMessageId);
            testServiceMessageInfoRO.setTimeReceived(userMessageLog.getReceived());
            testServiceMessageInfoRO.setPartyId(partyId);
            Party party = pModeProvider.getPartyByIdentifier(partyId);
            testServiceMessageInfoRO.setAccessPoint(party.getEndpoint());

            return ResponseEntity.ok().body(testServiceMessageInfoRO);
        }

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "test/incoming/latest", method = RequestMethod.GET)
    public ResponseEntity<TestServiceMessageInfoRO> getLastTestReceived(@RequestParam(value = "partyId") String partyId, @RequestParam(value = "userMessageId") String userMessageId) {
        LOG.debug("Getting last received test message from partyId='{}'", partyId);
        Messaging messaging = messagingDao.findMessageByMessageId(userMessageId);
        if(messaging == null) {
            LOG.debug("Could not find messaging for message ID[{}]", userMessageId);
            return ResponseEntity.noContent().build();
        }

        SignalMessage signalMessage = messaging.getSignalMessage();
        if (signalMessage != null) {
            TestServiceMessageInfoRO testServiceMessageInfoRO = new TestServiceMessageInfoRO();
            testServiceMessageInfoRO.setMessageId(signalMessage.getMessageInfo().getMessageId());
            testServiceMessageInfoRO.setTimeReceived(signalMessage.getMessageInfo().getTimestamp());
            Party party = pModeProvider.getPartyByIdentifier(partyId);
            testServiceMessageInfoRO.setPartyId(partyId);
            testServiceMessageInfoRO.setAccessPoint(party.getEndpoint());

            return ResponseEntity.ok().body(testServiceMessageInfoRO);
        }

        return ResponseEntity.noContent().build();
    }

    private HashMap<String, Object> createFilterMap(@RequestParam(value = "messageId", required = false) String messageId, @RequestParam(value = "conversationId", required = false) String conversationId, @RequestParam(value = "mshRole", required = false) MSHRole mshRole, @RequestParam(value = "messageStatus", required = false) MessageStatus messageStatus, @RequestParam(value = "notificationStatus", required = false) NotificationStatus notificationStatus, @RequestParam(value = "fromPartyId", required = false) String fromPartyId, @RequestParam(value = "toPartyId", required = false) String toPartyId, @RequestParam(value = "refToMessageId", required = false) String refToMessageId, @RequestParam(value = "originalSender", required = false) String originalSender, @RequestParam(value = "finalRecipient", required = false) String finalRecipient, @RequestParam(value = "messageSubtype") MessageSubtype messageSubtype) {
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("messageId", messageId);
        filters.put("conversationId", conversationId);
        filters.put("mshRole", mshRole);
        filters.put("messageStatus", messageStatus);
        filters.put("notificationStatus", notificationStatus);
        filters.put("fromPartyId", fromPartyId);
        filters.put("toPartyId", toPartyId);
        filters.put("refToMessageId", refToMessageId);
        filters.put("originalSender", originalSender);
        filters.put("finalRecipient", finalRecipient);
        filters.put("messageSubtype", messageSubtype);
        return filters;
    }

}

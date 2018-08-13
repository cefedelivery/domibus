package eu.domibus.common.services.impl;

import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.services.MessagesLogService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Service
public class MessagesLogServiceImpl implements MessagesLogService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagesLogServiceImpl.class);

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Override
    public List<? extends MessageLog> findMessageLogs(int page, int size, String column, boolean asc, HashMap<String, Object> filters) {

        List<? extends MessageLog> messageLogEntries;

        MessageType messageType = (MessageType) filters.get("messageType");

        switch (messageType) {
            case USER_MESSAGE:
                messageLogEntries = userMessageLogDao.findPaged(size * (page - 1), size, column, asc, filters);
                break;
            case SIGNAL_MESSAGE:
                messageLogEntries = signalMessageLogDao.findPaged(size * (page - 1), size, column, asc, filters);
                break;
            default:
                messageLogEntries = userMessageLogDao.findPaged(size * (page - 1), size, column, asc, filters);
        }
        return messageLogEntries;
    }

    @Override
    public Long countMessages(int size, HashMap<String, Object> filters) {

        long entries;

        MessageType messageType = (MessageType) filters.get("messageType");

        switch (messageType) {
            case USER_MESSAGE:
                entries = userMessageLogDao.countMessages(filters);
                break;
            case SIGNAL_MESSAGE:
                entries = signalMessageLogDao.countMessages(filters);
                break;
            default:
                entries = userMessageLogDao.countMessages(filters);
        }

        if (size <= 0) size = 10;
        long pages = entries / size;
        if (entries % size != 0) {
            pages++;
        }
        return pages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageLogResultRO countAndFindPaged(MessageType messageType, int from, int max, String column, boolean asc, Map<String, Object> filters) {
        MessageLogResultRO result = new MessageLogResultRO();

        List<MessageLogInfo> resultList = new ArrayList<>();
        if (messageType == MessageType.SIGNAL_MESSAGE) {
            int numberOfSignalMessageLogs = signalMessageLogDao.countAllInfo(asc, filters);
            LOG.debug("count Signal Messages Logs [{}]", numberOfSignalMessageLogs);
            result.setCount(numberOfSignalMessageLogs);
            resultList = signalMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);

        } else if (messageType == MessageType.USER_MESSAGE) {
            int numberOfUserMessageLogs = userMessageLogDao.countAllInfo(asc, filters);
            LOG.debug("count User Messages Logs [{}]", numberOfUserMessageLogs);
            result.setCount(numberOfUserMessageLogs);
            resultList = userMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
        }
        result.setMessageLogEntries(resultList
                .stream()
                .map(messageLogInfo -> convertMessageLogInfo(messageLogInfo))
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public List<MessageLogInfo> findAllInfoCSV(MessageType messageType, int max, Map<String, Object> filters) {

        return (messageType == MessageType.SIGNAL_MESSAGE ?
                signalMessageLogDao.findAllInfoPaged(0, max, null, true, filters) :
                userMessageLogDao.findAllInfoPaged(0, max, null, true, filters));
    }


    /**
     *
     * @param messageLogInfo
     * @return
     */
    MessageLogRO convertMessageLogInfo(MessageLogInfo messageLogInfo) {
        if (messageLogInfo == null) {
            return null;
        }

        return domainConverter.convert(messageLogInfo, MessageLogRO.class);
    }

}

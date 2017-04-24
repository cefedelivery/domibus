package eu.domibus.common.services.impl;

import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.services.MessagesLogService;
import eu.domibus.ebms3.common.model.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Service
public class MessagesLogServiceImpl implements MessagesLogService {

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

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

}

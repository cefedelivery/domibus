package eu.domibus.core.replication;

import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation for {@link UIMessageService}
 *
 * @author  Catalin Enache
 * @since 4.0
 */
@Service
public class UIMessageServiceImpl implements UIMessageService {

    @Autowired
    private UIMessageDao uiMessageDao;

    @Override
    public List<MessageLogInfo> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters) {
        return uiMessageDao.findPaged(from, max, column, asc, filters)
                .stream()
                .map(uiMessageEntity -> convertToMessageLogInfo(uiMessageEntity))
                .collect(Collectors.toList());
    }

    @Override
    public MessageLogResultRO countAndFindPaged(int from, int max, String column, boolean asc, Map<String, Object> filters) {
        MessageLogResultRO result = new MessageLogResultRO();

        //make the count
        int numberOfMessages = uiMessageDao.countMessages(filters);

        //query for the page results
        List<UIMessageEntity> uiMessageEntityList = uiMessageDao.findPaged(from, max, column, asc, filters);

        result.setCount(numberOfMessages);
        result.setMessageLogEntries(uiMessageEntityList
                .stream()
                .map(uiMessageEntity -> convertUIMessageEntity(uiMessageEntity))
                .collect(Collectors.toList()));

        return result;
    }


    /**
     * Converts {@link UIMessageEntity} object to {@link MessageLogRO} to be used on GUI
     *
     * @param uiMessageEntity
     * @return an {@link MessageLogRO} object
     */
    private MessageLogRO convertUIMessageEntity(UIMessageEntity uiMessageEntity) {
        if(uiMessageEntity == null) {
            return null;
        }

        MessageLogRO result = new MessageLogRO();
        result.setConversationId(uiMessageEntity.getConversationId());
        result.setFromPartyId(uiMessageEntity.getFromId());
        result.setToPartyId(uiMessageEntity.getToId());
        result.setOriginalSender(uiMessageEntity.getFromScheme());
        result.setFinalRecipient(uiMessageEntity.getToScheme());
        result.setRefToMessageId(uiMessageEntity.getRefToMessageId());
        result.setMessageId(uiMessageEntity.getMessageId());
        result.setMessageStatus(uiMessageEntity.getMessageStatus());
        result.setNotificationStatus(uiMessageEntity.getNotificationStatus());
        result.setMshRole(uiMessageEntity.getMshRole());
        result.setMessageType(uiMessageEntity.getMessageType());
        result.setDeleted(uiMessageEntity.getDeleted());
        result.setReceived(uiMessageEntity.getReceived());
        result.setSendAttempts(uiMessageEntity.getSendAttempts());
        result.setSendAttemptsMax(uiMessageEntity.getSendAttemptsMax());
        result.setNextAttempt(uiMessageEntity.getNextAttempt());
        result.setFailed(uiMessageEntity.getFailed());
        result.setRestored(uiMessageEntity.getRestored());
        result.setMessageSubtype(uiMessageEntity.getMessageSubtype());
        return result;
    }


    /**
     *
     * @param uiMessageEntity
     * @return
     */
    private MessageLogInfo convertToMessageLogInfo(UIMessageEntity uiMessageEntity) {
        if (uiMessageEntity == null) {
            return null;
        }

        return new MessageLogInfo(
                uiMessageEntity.getMessageId(),
                uiMessageEntity.getMessageStatus(),
                uiMessageEntity.getNotificationStatus(),
                uiMessageEntity.getMshRole(),
                uiMessageEntity.getMessageType(),
                uiMessageEntity.getDeleted(),
                uiMessageEntity.getReceived(),
                uiMessageEntity.getSendAttempts(),
                uiMessageEntity.getSendAttemptsMax(),
                uiMessageEntity.getNextAttempt(),
                uiMessageEntity.getConversationId(),
                uiMessageEntity.getFromId(),
                uiMessageEntity.getToId(),
                uiMessageEntity.getFromScheme(),
                uiMessageEntity.getToScheme(),
                uiMessageEntity.getRefToMessageId(),
                uiMessageEntity.getFailed(),
                uiMessageEntity.getRestored(),
                uiMessageEntity.getMessageSubtype()
        );
    }
}

package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.*;


/**
 * @param <F> MessageLog type: either UserMessageLog or SignalMessageLog
 * @author Federico Martini
 * @since 3.2
 */
public abstract class MessageLogDao<F extends MessageLog> extends BasicDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageLog.class);

    public MessageLogDao(final Class<F> type) {
        super(type);
    }

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void setMessageStatus(MessageLog messageLog, MessageStatus messageStatus) {
        messageLog.setMessageStatus(messageStatus);

        switch (messageStatus) {
            case DELETED:
            case ACKNOWLEDGED:
            case ACKNOWLEDGED_WITH_WARNING:
                messageLog.setDeleted(new Date());
                messageLog.setNextAttempt(null);
                break;
            case DOWNLOADED:
                messageLog.setDownloaded(new Date());
                messageLog.setNextAttempt(null);
                break;
            case SEND_FAILURE:
                messageLog.setFailed(new Date());
                messageLog.setNextAttempt(null);
                break;
            default:
        }
        super.update(messageLog);
        final String messageId = messageLog.getMessageId();
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_UPDATE, messageLog.getMessageType(), messageStatus);
    }

    public MessageStatus getMessageStatus(String messageId) {
        try {
            return findByMessageId(messageId).getMessageStatus();
        } catch (NoResultException nrEx) {
            LOG.debug("No result for message with id [" + messageId + "]");
            return MessageStatus.NOT_FOUND;
        }
    }

    protected abstract MessageLog findByMessageId(String messageId);

    protected abstract MessageLog findByMessageId(String messageId, MSHRole mshRole);

    protected abstract Long countMessages(Map<String, Object> filters);

    protected abstract List<F> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters);

    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<? extends MessageLog> mle) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                if (filter.getValue() instanceof String) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey()) {
                            case "":
                                break;
                            default:
                                predicates.add(cb.like(mle.get(filter.getKey()), (String) filter.getValue()));
                                break;
                        }
                    }
                } else if (filter.getValue() instanceof Date) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey()) {
                            case "receivedFrom":
                                predicates.add(cb.greaterThanOrEqualTo(mle.<Date>get("received"), Timestamp.valueOf(filter.getValue().toString())));
                                break;
                            case "receivedTo":
                                predicates.add(cb.lessThanOrEqualTo(mle.<Date>get("received"), Timestamp.valueOf(filter.getValue().toString())));
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    predicates.add(cb.equal(mle.<String>get(filter.getKey()), filter.getValue()));
                }
            }
        }
        return predicates;
    }

}

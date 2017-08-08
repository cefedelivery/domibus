package eu.domibus.api.message;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageLogService {

    void save(String messageId, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint);

    void setMessageAsDeleted(String messageId);

    void setMessageAsDownloaded(String messageId);

    void setMessageAsAcknowledged(String messageId);

    void setMessageAsAckWithWarnings(String messageId);

    void setMessageAsWaitingForReceipt(String messageId);

    void setMessageAsSendFailure(String messageId);

    void setIntermediaryPullStatus(String messageId);
}

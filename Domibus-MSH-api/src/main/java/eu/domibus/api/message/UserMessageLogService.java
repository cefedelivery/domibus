package eu.domibus.api.message;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageLogService {

    //TODO create a UserMessageLog business object and pass it to the save method
    void save(String messageId, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint, String action, String service, Boolean sourceMessage, Boolean messageFragment);

    void setMessageAsDeleted(String messageId);

    void setMessageAsDownloaded(String messageId);

    void setMessageAsAcknowledged(String messageId);

    void setMessageAsAckWithWarnings(String messageId);

    void setMessageAsWaitingForReceipt(String messageId);

    void setMessageAsSendFailure(String messageId);
}

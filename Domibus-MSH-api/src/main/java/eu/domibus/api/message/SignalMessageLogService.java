package eu.domibus.api.message;

/**
 * Interface for the Service class of SignalMessageLog
 * @author Tiago Miguel
 * @since 4.0
 */
public interface SignalMessageLogService {

    void save(String messageId, String userMessageService, String userMessageAction);
}

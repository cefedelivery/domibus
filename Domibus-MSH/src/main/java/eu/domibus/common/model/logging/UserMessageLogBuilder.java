package eu.domibus.common.model.logging;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;

/**
 * @author Federico Martini
 * @since 3.2
 */
public class UserMessageLogBuilder {

    private UserMessageLog userMessageLog;

    public static UserMessageLogBuilder create() {
        return new UserMessageLogBuilder();
    }

    private UserMessageLogBuilder() {
        this.userMessageLog = new UserMessageLog();
    }

    public UserMessageLog build() {
        return userMessageLog;
    }

    public UserMessageLogBuilder setMessageId(String messageId) {
        userMessageLog.setMessageId(messageId);
        return this;
    }

    public UserMessageLogBuilder setMessageStatus(MessageStatus messageStatus) {
        userMessageLog.setMessageStatus(messageStatus);
        return this;
    }

    public UserMessageLogBuilder setNotificationStatus(NotificationStatus notificationStatus) {
        userMessageLog.setNotificationStatus(notificationStatus);
        return this;
    }

    public UserMessageLogBuilder setMshRole(MSHRole mshRole) {
        userMessageLog.setMshRole(mshRole);
        return this;
    }

    public UserMessageLogBuilder setSendAttemptsMax(int maxAttempts) {
        userMessageLog.setSendAttemptsMax(maxAttempts);
        return this;
    }

    public UserMessageLogBuilder setMpc(String mpc) {
        userMessageLog.setMpc(mpc);
        return this;
    }

    public UserMessageLogBuilder setBackendName(String backendName) {
        userMessageLog.setBackend(backendName);
        return this;
    }

    public UserMessageLogBuilder setEndpoint(String endpoint) {
        userMessageLog.setEndpoint(endpoint);
        return this;
    }
}

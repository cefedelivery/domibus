package eu.domibus.common.model.logging;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;

/**
 * @author Federico Martini
 * @since 3.2
 */
public class UserMessageLogEntityBuilder {

    private UserMessageLog userMessageLog;

    public static UserMessageLogEntityBuilder create() {
        return new UserMessageLogEntityBuilder();
    }

    private UserMessageLogEntityBuilder() {
        this.userMessageLog = new UserMessageLog();
    }

    public UserMessageLog build() {
        return userMessageLog;
    }

    public UserMessageLogEntityBuilder setMessageId(String messageId) {
        userMessageLog.setMessageId(messageId);
        return this;
    }

    public UserMessageLogEntityBuilder setMessageStatus(MessageStatus messageStatus) {
        userMessageLog.setMessageStatus(messageStatus);
        return this;
    }

    public UserMessageLogEntityBuilder setNotificationStatus(NotificationStatus notificationStatus) {
        userMessageLog.setNotificationStatus(notificationStatus);
        return this;
    }

    public UserMessageLogEntityBuilder setMshRole(MSHRole mshRole) {
        userMessageLog.setMshRole(mshRole);
        return this;
    }

    public UserMessageLogEntityBuilder setSendAttemptsMax(int maxAttempts) {
        userMessageLog.setSendAttemptsMax(maxAttempts);
        return this;
    }

    public UserMessageLogEntityBuilder setMpc(String mpc) {
        userMessageLog.setMpc(mpc);
        return this;
    }

    public UserMessageLogEntityBuilder setBackendName(String backendName) {
        userMessageLog.setBackend(backendName);
        return this;
    }

    public UserMessageLogEntityBuilder setEndpoint(String endpoint) {
        userMessageLog.setEndpoint(endpoint);
        return this;
    }
}

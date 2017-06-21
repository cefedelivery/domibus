package eu.domibus.common.model.logging;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;

/**
 * @author Federico Martini
 * @since 3.2
 */
public class SignalMessageLogBuilder {

    private SignalMessageLog signalMessageLog;

    public static SignalMessageLogBuilder create() {
        return new SignalMessageLogBuilder();
    }

    private SignalMessageLogBuilder() {
        this.signalMessageLog = new SignalMessageLog();
    }

    public SignalMessageLog build() {
        return signalMessageLog;
    }

    public SignalMessageLogBuilder setMessageId(String messageId) {
        signalMessageLog.setMessageId(messageId);
        return this;
    }

    public SignalMessageLogBuilder setMessageStatus(MessageStatus messageStatus) {
        signalMessageLog.setMessageStatus(messageStatus);
        return this;
    }

    public SignalMessageLogBuilder setNotificationStatus(NotificationStatus notificationStatus) {
        signalMessageLog.setNotificationStatus(notificationStatus);
        return this;
    }

    public SignalMessageLogBuilder setMshRole(MSHRole mshRole) {
        signalMessageLog.setMshRole(mshRole);
        return this;
    }

}

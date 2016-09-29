package eu.domibus.common.model.logging;

import eu.domibus.ebms3.common.model.MessageType;

import javax.persistence.*;
import java.util.Date;


/**
 * @author Federico Martini
 * @since 3.2
 */
@Entity
@Table(name = "TB_MESSAGE_LOG")
@DiscriminatorValue("SIGNAL_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "SignalMessageLog.findUndeletedMessages",
                query = "select mle.messageId from SignalMessageLog mle where mle.deleted is null and mle.mshRole=:MSH_ROLE and mle.messageType=:MESSAGE_TYPE"),
        @NamedQuery(name = "SignalMessageLog.findRetryMessages", query = "select mle.messageId from SignalMessageLog mle where mle.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY and mle.nextAttempt < CURRENT_TIMESTAMP and 1 <= mle.sendAttempts and mle.sendAttempts <= mle.sendAttemptsMax"),
        @NamedQuery(name = "SignalMessageLog.findTimedoutMessages", query = "select mle.messageId from SignalMessageLog mle where mle.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY and mle.nextAttempt < :TIMESTAMP_WITH_TOLERANCE"),
        @NamedQuery(name = "SignalMessageLog.findByMessageId", query = "select mle from SignalMessageLog mle where mle.messageId=:MESSAGE_ID and mle.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "SignalMessageLog.findBackendForMessage", query = "select mle.backend from SignalMessageLog mle where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.setMessageStatus",
                query = "update SignalMessageLog mle set mle.deleted=:TIMESTAMP, mle.messageStatus=:MESSAGE_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.setMessageStatusAndNotificationStatus",
                query = "update SignalMessageLog mle set mle.deleted=:TIMESTAMP, mle.messageStatus=:MESSAGE_STATUS, mle.notificationStatus=:NOTIFICATION_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.getMessageStatus", query = "select mle.messageStatus from SignalMessageLog  mle where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.findEntries", query = "select mle from SignalMessageLog mle"),
        @NamedQuery(name = "SignalMessageLog.findUndownloadedUserMessagesOlderThan", query = "select mle.messageId from SignalMessageLog mle where (mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and mle.deleted is null and mle.mpc = :MPC and mle.received < :DATE"),
        @NamedQuery(name = "SignalMessageLog.findDownloadedUserMessagesOlderThan", query = "select mle.messageId from SignalMessageLog mle where (mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and mle.mpc = :MPC and mle.deleted is not null and mle.deleted < :DATE"),
        @NamedQuery(name = "SignalMessageLog.findEndpointForId", query = "select mle.endpoint from SignalMessageLog mle where mle.messageId =:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.setNotificationStatus", query = "update SignalMessageLog mle set mle.notificationStatus=:NOTIFICATION_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.countEntries", query = "select count(mle.messageId) from SignalMessageLog mle")})
public class SignalMessageLog extends MessageLog {

    public SignalMessageLog() {
        setMessageType(MessageType.SIGNAL_MESSAGE);
        setReceived(new Date());
        setNextAttempt(getReceived());
    }


}



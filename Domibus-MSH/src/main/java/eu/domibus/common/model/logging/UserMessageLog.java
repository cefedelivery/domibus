/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.domibus.common.model.logging;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Entity
@Table(name = "TB_MESSAGE_LOG")
@DiscriminatorValue("USER_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "UserMessageLog.findUndeletedMessages",
                query = "select mle.messageId from UserMessageLog mle where mle.deleted is null and mle.mshRole=:MSH_ROLE and mle.messageType=:MESSAGE_TYPE"),
        @NamedQuery(name = "UserMessageLog.findRetryMessages", query = "select mle.messageId from UserMessageLog mle where mle.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY and mle.nextAttempt < CURRENT_TIMESTAMP and 1 <= mle.sendAttempts and mle.sendAttempts <= mle.sendAttemptsMax"),
        @NamedQuery(name = "UserMessageLog.findTimedoutMessages", query = "select mle.messageId from UserMessageLog mle where mle.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY and mle.nextAttempt < :TIMESTAMP_WITH_TOLERANCE"),
        @NamedQuery(name = "UserMessageLog.findByMessageId", query = "select mle from UserMessageLog mle where mle.messageId=:MESSAGE_ID and mle.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.findBackendForMessage", query = "select mle.backend from UserMessageLog mle where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.setMessageStatus",
                query = "update UserMessageLog mle set mle.deleted=:TIMESTAMP, mle.messageStatus=:MESSAGE_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.setMessageStatusAndNotificationStatus",
                query = "update UserMessageLog mle set mle.deleted=:TIMESTAMP, mle.messageStatus=:MESSAGE_STATUS, mle.notificationStatus=:NOTIFICATION_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.getMessageStatus", query = "select mle.messageStatus from UserMessageLog  mle where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findEntries", query = "select mle from UserMessageLog mle"),
        @NamedQuery(name = "UserMessageLog.findUndownloadedUserMessagesOlderThan", query = "select mle.messageId from UserMessageLog mle where (mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and mle.deleted is null and mle.mpc = :MPC and mle.received < :DATE"),
        @NamedQuery(name = "UserMessageLog.findDownloadedUserMessagesOlderThan", query = "select mle.messageId from UserMessageLog mle where (mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and mle.mpc = :MPC and mle.deleted is not null and mle.deleted < :DATE"),
        @NamedQuery(name = "UserMessageLog.findEndpointForId", query = "select mle.endpoint from UserMessageLog mle where mle.messageId =:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.setNotificationStatus", query = "update UserMessageLog mle set mle.notificationStatus=:NOTIFICATION_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.countEntries", query = "select count(mle.messageId) from UserMessageLog mle")})
public class UserMessageLog extends MessageLog {

    public UserMessageLog() {
        setMessageType(MessageType.USER_MESSAGE);
    }

    public UserMessageLog(String messageId, MessageStatus messageStatus, NotificationStatus notificationStatus, MSHRole mshRole, String mpc, String backend, String endpoint, int sendAttemptsMax) {
        this();
        setMessageId(messageId);
        setMessageStatus(messageStatus);
        setNotificationStatus(notificationStatus);
        setMshRole(mshRole);
        setMpc(mpc);
        setBackend(backend);
        setEndpoint(endpoint);
        setReceived(new Date());
        setDeleted(null);
        setNextAttempt(getReceived());
        setSendAttempts(0);
        setSendAttemptsMax(sendAttemptsMax);
    }


}

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
        @NamedQuery(name = "UserMessageLog.findRetryMessages", query = "select userMessageLog.messageId from UserMessageLog userMessageLog where userMessageLog.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY and userMessageLog.nextAttempt < CURRENT_TIMESTAMP and 1 <= userMessageLog.sendAttempts and userMessageLog.sendAttempts <= userMessageLog.sendAttemptsMax"),
        @NamedQuery(name = "UserMessageLog.findTimedoutMessages", query = "select userMessageLog.messageId from UserMessageLog userMessageLog where userMessageLog.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY and userMessageLog.nextAttempt < :TIMESTAMP_WITH_TOLERANCE"),
        @NamedQuery(name = "UserMessageLog.findByMessageId", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageIdAndRole", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID and userMessageLog.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.findBackendForMessage", query = "select userMessageLog.backend from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findEntries", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findUndownloadedUserMessagesOlderThan", query = "select userMessageLog.messageId from UserMessageLog userMessageLog where (userMessageLog.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or userMessageLog.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and userMessageLog.mpc = :MPC and userMessageLog.deleted is null and userMessageLog.received < :DATE"),
        @NamedQuery(name = "UserMessageLog.findDownloadedUserMessagesOlderThan", query = "select userMessageLog.messageId from UserMessageLog userMessageLog where (userMessageLog.messageStatus = eu.domibus.common.MessageStatus.DOWNLOADED and userMessageLog.mpc = :MPC and userMessageLog.deleted is null and userMessageLog.received < :DATE)"),
        @NamedQuery(name = "UserMessageLog.setNotificationStatus", query = "update UserMessageLog userMessageLog set userMessageLog.notificationStatus=:NOTIFICATION_STATUS where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.countEntries", query = "select count(userMessageLog.messageId) from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.setMessageStatusAndNotificationStatus",
                query = "update UserMessageLog userMessageLog set userMessageLog.deleted=:TIMESTAMP, userMessageLog.messageStatus=:MESSAGE_STATUS, userMessageLog.notificationStatus=:NOTIFICATION_STATUS where userMessageLog.messageId=:MESSAGE_ID")
})
public class UserMessageLog extends MessageLog {

    public UserMessageLog() {

        setMessageType(MessageType.USER_MESSAGE);
        setReceived(new Date());
        setNextAttempt(getReceived());
        setSendAttempts(0);
    }

}

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
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import eu.domibus.ebms3.common.model.MessageType;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Entity
@Table(name = "TB_MESSAGE_LOG")
@NamedQueries({
        @NamedQuery(name = "MessageLogEntry.findUndeletedMessages",
                query = "select mle.messageId from MessageLogEntry mle where mle.deleted is null and mle.mshRole=:MSH_ROLE and mle.messageType=:MESSAGE_TYPE"),
        @NamedQuery(name = "MessageLogEntry.findRetryMessages", query = "select mle.messageId from MessageLogEntry mle where mle.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY and mle.nextAttempt < CURRENT_TIMESTAMP and 1 <= mle.sendAttempts and mle.sendAttempts <= mle.sendAttemptsMax"),
        @NamedQuery(name = "MessageLogEntry.findTimedoutMessages", query = "select mle.messageId from MessageLogEntry mle where mle.messageStatus = eu.domibus.common.MessageStatus.WAITING_FOR_RETRY and mle.nextAttempt < :TIMESTAMP_WITH_TOLERANCE"),
        @NamedQuery(name = "MessageLogEntry.findByMessageId", query = "select mle from MessageLogEntry mle where mle.messageId=:MESSAGE_ID and mle.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "MessageLogEntry.findBackendForMessage", query = "select mle.backend from MessageLogEntry mle where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessageLogEntry.setMessageStatus",
                query = "update MessageLogEntry mle set mle.deleted=:TIMESTAMP, mle.messageStatus=:MESSAGE_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessageLogEntry.setMessageStatusAndNotificationStatus",
                query = "update MessageLogEntry mle set mle.deleted=:TIMESTAMP, mle.messageStatus=:MESSAGE_STATUS, mle.notificationStatus=:NOTIFICATION_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessageLogEntry.getMessageStatus", query = "select mle.messageStatus from MessageLogEntry  mle where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessageLogEntry.findEntries", query = "select mle from MessageLogEntry mle"),
        @NamedQuery(name = "MessageLogEntry.findUndownloadedUserMessagesOlderThan", query = "select mle.messageId from MessageLogEntry mle where (mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and mle.deleted is null and mle.mpc = :MPC and mle.received < :DATE"),
        @NamedQuery(name = "MessageLogEntry.findDownloadedUserMessagesOlderThan", query = "select mle.messageId from MessageLogEntry mle where (mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED or mle.messageStatus = eu.domibus.common.MessageStatus.RECEIVED_WITH_WARNINGS) and mle.mpc = :MPC and mle.deleted is not null and mle.deleted < :DATE"),
        @NamedQuery(name = "MessageLogEntry.findEndpointForId", query = "select mle.endpoint from MessageLogEntry mle where mle.messageId =:MESSAGE_ID"),
        @NamedQuery(name = "MessageLogEntry.setNotificationStatus", query = "update MessageLogEntry mle set mle.notificationStatus=:NOTIFICATION_STATUS where mle.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "MessageLogEntry.countEntries", query = "select count(mle.messageId) from MessageLogEntry mle")})
public class MessageLogEntry extends AbstractBaseEntity {

    @Column(name = "MESSAGE_STATUS")
    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;
    @Column(name = "NOTIFICATION_STATUS")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;
    @Column(name = "MESSAGE_ID")
    private String messageId;
    @Enumerated(EnumType.STRING)
    @Column(name = "MSH_ROLE")
    private MSHRole mshRole;
    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_TYPE")
    private MessageType messageType;
    @Column(name = "MPC")
    private String mpc;
    @Column(name = "BACKEND")
    private String backend;
    @Column(name = "ENDPOINT")
    private String endpoint;
    /**
     * The Date when this message was deleted, A message shall be deleted when one of the following conditions apply:
     * <p/>
     * - An outgoing message has been sent without error eb:Error/@severity failure failure, and an AS4 receipt has been
     * received
     * - An outgoing message has been sent without error eb:Error/@severity failure, and AS4 is disabled
     * - An outgoing message could not be sent and the final AS4 retry has passed
     * - An outgoing message could not be sent and AS4 is disabled (eb:Error/@severity failure, [CORE 6.2.5])
     * <p/>
     * - A received message
     */
    @Column(name = "DELETED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;
    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;
    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;
    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;
    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMpc() {
        return this.mpc;
    }

    public void setMpc(final String mpc) {
        this.mpc = mpc;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public void setMessageType(final MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(final String messageId) {
        this.messageId = messageId;
    }

    public MSHRole getMshRole() {
        return this.mshRole;
    }

    public void setMshRole(final MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public Date getDeleted() {
        return this.deleted;
    }

    public void setDeleted(final Date deleted) {
        this.deleted = deleted;
    }

    public Date getReceived() {
        return this.received;
    }

    public void setReceived(final Date received) {
        this.received = received;
    }

    public Date getNextAttempt() {
        return this.nextAttempt;
    }

    public void setNextAttempt(final Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public int getSendAttempts() {
        return this.sendAttempts;
    }

    public void setSendAttempts(final int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public int getSendAttemptsMax() {
        return this.sendAttemptsMax;
    }

    public void setSendAttemptsMax(final int sendAttemptsMax) {
        this.sendAttemptsMax = sendAttemptsMax;
    }

    public NotificationStatus getNotificationStatus() {
        return this.notificationStatus;
    }

    public void setNotificationStatus(final NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public MessageStatus getMessageStatus() {
        return this.messageStatus;
    }

    public void setMessageStatus(final MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(final String backend) {
        this.backend = backend;
    }
}

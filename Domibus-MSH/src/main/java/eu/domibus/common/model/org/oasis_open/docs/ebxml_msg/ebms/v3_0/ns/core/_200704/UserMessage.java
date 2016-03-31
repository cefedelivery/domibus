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

package eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704;

import eu.domibus.common.model.AbstractBaseEntity;
import eu.domibus.common.model.configuration.Mpc;

import javax.persistence.*;
import javax.xml.bind.annotation.*;

/**
 * This element has the following attributes:
 * • eb:Messaging/eb:UserMessage/@mpc: This OPTIONAL attribute contains a URI that
 * identifies the Message Partition Channel to which the message is assigned. The absence of this
 * element indicates the use of the default MPC. When the message is pulled, the value of this
 * attribute MUST indicate the MPC requested in the PullRequest message.
 * This element has the following children elements:
 * • eb:Messaging/eb:UserMessage/eb:MessageInfo: This REQUIRED element occurs once,
 * and contains data that identifies the message, and relates to other messages' identifiers.
 * • eb:Messaging/eb:UserMessage/eb:PartyInfo: This REQUIRED element occurs once,
 * and contains data about originating party and destination party.
 * • eb:Messaging/eb:UserMessage/eb:CollaborationInfo: This REQUIRED element
 * occurs once, and contains elements that facilitate collaboration between parties.
 * • eb:Messaging/eb:UserMessage/eb:MessageProperties: This OPTIONAL element
 * occurs at most once, and contains message properties that are user-specific. As parts of the
 * header such properties allow for more efficient monitoring, correlating, dispatching and validating
 * functions (even if these are out of scope of ebMS specification) which would otherwise require
 * payload access.
 * • eb:Messaging/eb:UserMessage/eb:PayloadInfo: This OPTIONAL element occurs at
 * most once, and identifies payload data associated with the message, whether included as part of
 * the message as payload document(s) contained in a Payload Container, or remote resources
 * accessible via a URL. The purpose of the PayloadInfo is (a) to make it easier to directly extract a
 * particular payload associated with this User message, (b) to allow an application to determine
 * whether it can process the payload without having to parse it.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserMessage",
        propOrder = {"messageInfo", "partyInfo", "collaborationInfo", "messageProperties", "payloadInfo"})
@Entity
@Table(name = "TB_USER_MESSAGE")
public class UserMessage extends AbstractBaseEntity {

    @XmlElement(name = "MessageInfo", required = true)
    @OneToOne(cascade = CascadeType.ALL)
    protected MessageInfo messageInfo;
    @XmlElement(name = "PartyInfo", required = true)
    @Embedded
    protected PartyInfo partyInfo;
    @XmlElement(name = "CollaborationInfo", required = true)
    @Embedded
    protected CollaborationInfo collaborationInfo;
    @XmlElement(name = "MessageProperties")
    @Embedded
    protected MessageProperties messageProperties;
    @XmlElement(name = "PayloadInfo")
    @Embedded
    protected PayloadInfo payloadInfo;
    @XmlAttribute(name = "mpc")
    @XmlSchemaType(name = "anyURI")
    @Column(name = "MPC")
    protected String mpc = Mpc.DEFAULT_MPC;

    /**
     * This REQUIRED element occurs once,
     * and contains data that identifies the message, and relates to other messages' identifiers.
     *
     * @return possible object is {@link MessageInfo }
     */
    public MessageInfo getMessageInfo() {
        if (this.messageInfo == null) {
            this.messageInfo = new MessageInfo();
        }
        return this.messageInfo;
    }

    /**
     * This REQUIRED element occurs once,
     * and contains data that identifies the message, and relates to other messages' identifiers.
     *
     * @param value allowed object is {@link MessageInfo }
     */
    public void setMessageInfo(final MessageInfo value) {
        this.messageInfo = value;
    }

    /**
     * This REQUIRED element occurs once,
     * and contains data about originating party and destination party.
     *
     * @return possible object is {@link PartyInfo }
     */
    public PartyInfo getPartyInfo() {
        return this.partyInfo;
    }

    /**
     * This REQUIRED element occurs once,
     * and contains data about originating party and destination party.
     *
     * @param value allowed object is {@link PartyInfo }
     */
    public void setPartyInfo(final PartyInfo value) {
        this.partyInfo = value;
    }

    /**
     * This REQUIRED element
     * occurs once, and contains elements that facilitate collaboration between parties.
     *
     * @return possible object is {@link CollaborationInfo }
     */
    public CollaborationInfo getCollaborationInfo() {
        return this.collaborationInfo;
    }

    /**
     * This REQUIRED element
     * occurs once, and contains elements that facilitate collaboration between parties.
     *
     * @param value allowed object is {@link CollaborationInfo }
     */
    public void setCollaborationInfo(final CollaborationInfo value) {
        this.collaborationInfo = value;
    }

    /**
     * This OPTIONAL element
     * occurs at most once, and contains message properties that are user-specific. As parts of the
     * header such properties allow for more efficient monitoring, correlating, dispatching and validating
     * functions (even if these are out of scope of ebMS specification) which would otherwise require
     * payload access.
     *
     * @return possible object is {@link MessageProperties }
     */
    public MessageProperties getMessageProperties() {
        return this.messageProperties;
    }

    /**
     * This OPTIONAL element
     * occurs at most once, and contains message properties that are user-specific. As parts of the
     * header such properties allow for more efficient monitoring, correlating, dispatching and validating
     * functions (even if these are out of scope of ebMS specification) which would otherwise require
     * payload access.
     *
     * @param value allowed object is {@link MessageProperties }
     */
    public void setMessageProperties(final MessageProperties value) {
        this.messageProperties = value;
    }

    /**
     * This OPTIONAL element occurs at
     * most once, and identifies payload data associated with the message, whether included as part of
     * the message as payload document(s) contained in a Payload Container, or remote resources
     * accessible via a URL. The purpose of the PayloadInfo is (a) to make it easier to directly extract a
     * particular payload associated with this User message, (b) to allow an application to determine
     * whether it can process the payload without having to parse it.
     *
     * @return possible object is {@link PayloadInfo }
     */
    public PayloadInfo getPayloadInfo() {
        return this.payloadInfo;
    }

    /**
     * This OPTIONAL element occurs at
     * most once, and identifies payload data associated with the message, whether included as part of
     * the message as payload document(s) contained in a Payload Container, or remote resources
     * accessible via a URL. The purpose of the PayloadInfo is (a) to make it easier to directly extract a
     * particular payload associated with this User message, (b) to allow an application to determine
     * whether it can process the payload without having to parse it.
     *
     * @param value allowed object is {@link PayloadInfo }
     */
    public void setPayloadInfo(final PayloadInfo value) {
        this.payloadInfo = value;
    }

    /**
     * This OPTIONAL attribute contains a URI that
     * identifies the Message Partition Channel to which the message is assigned. The absence of this
     * element indicates the use of the default MPC. When the message is pulled, the value of this
     * attribute MUST indicate the MPC requested in the PullRequest message.
     *
     * @return possible object is {@link String }
     */
    public String getMpc() {
        return this.mpc;
    }

    /**
     * This OPTIONAL attribute contains a URI that
     * identifies the Message Partition Channel to which the message is assigned. The absence of this
     * element indicates the use of the default MPC. When the message is pulled, the value of this
     * attribute MUST indicate the MPC requested in the PullRequest message.
     *
     * @param value allowed object is {@link String }
     */
    public void setMpc(final String value) {
        this.mpc = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof UserMessage)) return false;

        final UserMessage that = (UserMessage) o;

        if (!this.collaborationInfo.equals(that.collaborationInfo)) return false;
        if (!this.messageInfo.equals(that.messageInfo)) return false;
        if (this.messageProperties != null ? !this.messageProperties.equals(that.messageProperties) : that.messageProperties != null)
            return false;
        if (this.mpc != null ? !this.mpc.equals(that.mpc) : that.mpc != null) return false;
        if (!this.partyInfo.equals(that.partyInfo)) return false;
        return !(this.payloadInfo != null ? !this.payloadInfo.equals(that.payloadInfo) : that.payloadInfo != null);

    }

    @Override
    public int hashCode() {
        int result = 31;
        result = 31 * result + this.messageInfo.hashCode();
        result = 31 * result + this.partyInfo.hashCode();
        result = 31 * result + this.collaborationInfo.hashCode();
        result = 31 * result + (this.messageProperties != null ? this.messageProperties.hashCode() : 0);
        result = 31 * result + (this.payloadInfo != null ? this.payloadInfo.hashCode() : 0);
        result = 31 * result + (this.mpc != null ? this.mpc.hashCode() : 0);
        return result;
    }
}

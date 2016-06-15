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


import eu.domibus.common.xmladapter.XMLGregorianCalendarAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

/**
 * This element has the following children elements:
 * • eb:Messaging/eb:UserMessage/eb:MessageInfo/eb:Timestamp: The REQUIRED
 * Timestamp element has a value representing the date at which the message header was created,
 * and is conforming to a dateTime (see [XMLSCHEMA]). It MUST be expressed as UTC. Indicating
 * UTC in the Timestamp element by including the 'Z' identifier is optional.
 * • eb:Messaging/eb:UserMessage/eb:MessageInfo/eb:MessageId: This REQUIRED
 * element has a value representing – for each message - a globally unique identifier conforming to
 * MessageId [RFC2822]. Note: In the Message-Id and Content-Id MIME headers, values are always
 * surrounded by angle brackets. However references in mid: or cid: scheme URI's and the
 * MessageId and RefToMessageId elements MUST NOT include these delimiters.
 * • eb:Messaging/eb:UserMessage/eb:MessageInfo/eb:RefToMessageId: This
 * OPTIONAL element occurs at most once. When present, it MUST contain the MessageId value of
 * an ebMS Message to which this message relates, in a way that conforms to the MEP in use (see
 * Section C.3).
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageInfo", propOrder = {"timestamp", "messageId", "refToMessageId"})

public class MessageInfo {

    public static final String MESSAGE_ID_CONTEXT_PROPERTY = "ebms.messageid";

    @XmlElement(name = "Timestamp", required = true)
    @XmlSchemaType(name = "dateTime")

    @XmlJavaTypeAdapter(XMLGregorianCalendarAdapter.class)

    protected Date timestamp;
    @XmlElement(name = "MessageId", required = true)

    protected String messageId;
    @XmlElement(name = "RefToMessageId")

    protected String refToMessageId;

    /**
     * The REQUIRED
     * Timestamp element has a value representing the date at which the message header was created,
     * and is conforming to a dateTime (see [XMLSCHEMA]). It MUST be expressed as UTC. Indicating
     * UTC in the Timestamp element by including the 'Z' identifier is optional.
     *
     * @return possible object is {@link Date }
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * The REQUIRED
     * Timestamp element has a value representing the date at which the message header was created,
     * and is conforming to a dateTime (see [XMLSCHEMA]). It MUST be expressed as UTC. Indicating
     * UTC in the Timestamp element by including the 'Z' identifier is optional.
     *
     * @param value allowed object is {@link Date }
     */
    public void setTimestamp(final Date value) {
        this.timestamp = value;
    }

    /**
     * This REQUIRED
     * element has a value representing – for each message - a globally unique identifier conforming to
     * MessageId [RFC2822]. Note: In the Message-Id and Content-Id MIME headers, values are always
     * surrounded by angle brackets. However references in mid: or cid: scheme URI's and the
     * MessageId and RefToMessageId elements MUST NOT include these delimiters.
     *
     * @return possible object is {@link String }
     */
    public String getMessageId() {
        return this.messageId;
    }

    /**
     * This REQUIRED
     * element has a value representing – for each message - a globally unique identifier conforming to
     * MessageId [RFC2822]. Note: In the Message-Id and Content-Id MIME headers, values are always
     * surrounded by angle brackets. However references in mid: or cid: scheme URI's and the
     * MessageId and RefToMessageId elements MUST NOT include these delimiters.
     *
     * @param value allowed object is {@link String }
     */
    public void setMessageId(final String value) {
        this.messageId = value;
    }

    /**
     * This
     * OPTIONAL element occurs at most once. When present, it MUST contain the MessageId value of
     * an ebMS Message to which this message relates, in a way that conforms to the MEP in use (see
     * Section C.3).
     *
     * @return possible object is {@link String }
     */
    public String getRefToMessageId() {
        return this.refToMessageId;
    }

    /**
     * This
     * OPTIONAL element occurs at most once. When present, it MUST contain the MessageId value of
     * an ebMS Message to which this message relates, in a way that conforms to the MEP in use (see
     * Section C.3).
     *
     * @param value allowed object is {@link String }
     */
    public void setRefToMessageId(final String value) {
        this.refToMessageId = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageInfo)) return false;

        final MessageInfo that = (MessageInfo) o;

        if (!this.messageId.equals(that.messageId)) return false;
        if (this.refToMessageId != null ? !this.refToMessageId.equals(that.refToMessageId) : that.refToMessageId != null)
            return false;
        return this.timestamp.equals(that.timestamp);

    }

    @Override
    public int hashCode() {
        int result = 31;
        result = 31 * result + this.timestamp.hashCode();
        result = 31 * result + this.messageId.hashCode();
        result = 31 * result + (this.refToMessageId != null ? this.refToMessageId.hashCode() : 0);
        return result;
    }
}

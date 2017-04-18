package eu.domibus.api.message.ebms3.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
@Entity
@Table(name = "TB_MESSAGE_INFO")
public class MessageInfo extends AbstractBaseEntity {

    public static final String MESSAGE_ID_CONTEXT_PROPERTY = "ebms.messageid";

    @XmlElement(name = "Timestamp", required = true)
    @XmlSchemaType(name = "dateTime")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(XMLGregorianCalendarAdapter.class)
    @Column(name = "TIME_STAMP")
    protected Date timestamp;
    @XmlElement(name = "MessageId", required = true)
    @Column(name = "MESSAGE_ID", nullable = false, unique = true, updatable = false)
    @NotNull
    protected String messageId;
    @XmlElement(name = "RefToMessageId")
    @Column(name = "REF_TO_MESSAGE_ID")
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
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageInfo that = (MessageInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(timestamp, that.timestamp)
                .append(messageId, that.messageId)
                .append(refToMessageId, that.refToMessageId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(timestamp)
                .append(messageId)
                .append(refToMessageId)
                .toHashCode();
    }
}

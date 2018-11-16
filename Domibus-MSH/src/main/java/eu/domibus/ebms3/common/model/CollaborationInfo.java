package eu.domibus.ebms3.common.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.UUID;

/**
 * This REQUIRED element
 * occurs once, and contains elements that facilitate collaboration between parties.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollaborationInfo", propOrder = {"agreementRef", "service", "action", "conversationId"})

@Embeddable
public class CollaborationInfo {

    @XmlElement(name = "ConversationId", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    @Column(name = "COLL_INFO_CONVERS_ID", nullable = false)
    @NotNull
    protected String conversationId = UUID.randomUUID().toString();
    @XmlElement(name = "Action", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    @Column(name = "COLLABORATION_INFO_ACTION")
    @NotNull
    protected String action = Ebms3Constants.TEST_ACTION;
    @XmlElement(name = "AgreementRef")
    //Embedable
    protected AgreementRef agreementRef;
    @XmlElement(name = "Service", required = true)
    //Embedable
    protected Service service;

    /**
     * OPTIONAL element occurs zero or once. The AgreementRef element is an object that identifies the
     * entity or artifact governing the exchange of messages between the parties.
     *
     * @return possible object is {@link AgreementRef }
     */
    public AgreementRef getAgreementRef() {
        return this.agreementRef;
    }

    /**
     * OPTIONAL element occurs zero or once. The AgreementRef element is an object that identifies the
     * entity or artifact governing the exchange of messages between the parties.
     *
     * @param value allowed object is {@link AgreementRef }
     */
    public void setAgreementRef(final AgreementRef value) {
        this.agreementRef = value;
    }

    /**
     * This
     * REQUIRED element occurs once. It is a string identifying the service that acts on the message
     * and it is specified by the designer of the service.
     *
     * @return possible object is {@link Service }
     */
    public Service getService() {
        return this.service;
    }

    /**
     * This
     * REQUIRED element occurs once. It is a string identifying the service that acts on the message
     * and it is specified by the designer of the service.
     *
     * @param value allowed object is {@link Service }
     */
    public void setService(final Service value) {
        this.service = value;
    }

    /**
     * This REQUIRED
     * element occurs once. The element is a string identifying an operation or an activity within a
     * Service that may support several of these. Its actual semantics is
     * beyond the scope of this specification. Action SHALL be unique within the Service in which it is defined.
     * The value of the Action element is specified by the designer of the service.
     * An example of the Action element follows:
     * {@code <eb:Action>NewOrder</eb:Action>}
     * If the value of either the Service or Action element is unrecognized by the Receiving MSH, then it MUST
     * report a "ValueNotRecognized" error of severity "error".
     * When the value of this element is http://docs.oasis-open.org/ebxmlmsg/
     * ebms/v3.0/ns/core/200704/test, then the eb:Service element MUST have the value
     * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service. Such a
     * value for the eb:Action element only indicates that the user message is sent for testing purposes and does
     * not require any specific handling by the MSH.
     *
     * @return possible object is {@link String }
     */
    public String getAction() {
        return this.action;
    }

    /**
     * This REQUIRED
     * element occurs once. The element is a string identifying an operation or an activity within a
     * Service that may support several of these. Its actual semantics is
     * beyond the scope of this specification. Action SHALL be unique within the Service in which it is defined.
     * The value of the Action element is specified by the designer of the service.
     * An example of the Action element follows:
     * {@code <eb:Action>NewOrder</eb:Action>}
     * If the value of either the Service or Action element is unrecognized by the Receiving MSH, then it MUST
     * report a "ValueNotRecognized" error of severity "error".
     * When the value of this element is http://docs.oasis-open.org/ebxmlmsg/
     * ebms/v3.0/ns/core/200704/test, then the eb:Service element MUST have the value
     * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service. Such a
     * value for the eb:Action element only indicates that the user message is sent for testing purposes and does
     * not require any specific handling by the MSH.
     *
     * @param value allowed object is {@link String }
     */
    //TODO: respect test action
    public void setAction(final String value) {
        this.action = value;
    }

    /**
     * This element is a string identifying the set of related messages that make up a conversation between
     * Parties.
     * If a CPA is referred to by eb:AgreementRef, the number of conversations related to this CPA MUST
     * comply with CPA requirements. The value of eb:ConversationId MUST uniquely identify a
     * conversation within the context of this CPA.
     * An example of the ConversationId element follows:
     * {@code <eb:ConversationId>20001209-133003-28572</eb:ConversationId>}
     * The Party initiating a conversation determines the value of the ConversationId element that SHALL be
     * reflected in all messages pertaining to that conversation. The actual semantics of this value is beyond the
     * scope of this specification. Implementations SHOULD provide a facility for mapping between their
     * identification scheme and a ConversationId generated by another implementation.
     *
     * @return possible object is {@link String }
     */
    public String getConversationId() {
        // this is because Oracle treats empty string as null
        // if we get space, we transform it as an empty string
        return StringUtils.SPACE.equals(this.conversationId) ? StringUtils.EMPTY : this.conversationId;
    }

    /**
     * This element is a string identifying the set of related messages that make up a conversation between
     * Parties.
     * If a CPA is referred to by eb:AgreementRef, the number of conversations related to this CPA MUST
     * comply with CPA requirements. The value of eb:ConversationId MUST uniquely identify a
     * conversation within the context of this CPA.
     * An example of the ConversationId element follows:
     * {@code <eb:ConversationId>20001209-133003-28572</eb:ConversationId>}
     * The Party initiating a conversation determines the value of the ConversationId element that SHALL be
     * reflected in all messages pertaining to that conversation. The actual semantics of this value is beyond the
     * scope of this specification. Implementations SHOULD provide a facility for mapping between their
     * identification scheme and a ConversationId generated by another implementation.
     *
     * @param value allowed object is {@link String }
     */
    //TODO: check for conversationId uniqueness within a msh
    //TODO: allow conversationId specification within a msh (regex?)
    public void setConversationId(final String value) {
        // this is because Oracle treats empty strings as null
        // if we receive an empty string, we transform it to space
        this.conversationId = StringUtils.EMPTY.equals(value) ? StringUtils.SPACE : value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CollaborationInfo)) return false;

        final CollaborationInfo that = (CollaborationInfo) o;

        if (!this.action.equalsIgnoreCase(that.action)) return false;
        if (this.agreementRef != null ? !this.agreementRef.equals(that.agreementRef) : that.agreementRef != null)
            return false;
        if (!this.conversationId.equalsIgnoreCase(that.conversationId)) return false;
        return this.service.equals(that.service);

    }

    @Override
    public int hashCode() {
        int result = this.conversationId.hashCode();
        result = 31 * result + this.action.hashCode();
        result = 31 * result + (this.agreementRef != null ? this.agreementRef.hashCode() : 0);
        result = 31 * result + this.service.hashCode();
        return result;
    }
}
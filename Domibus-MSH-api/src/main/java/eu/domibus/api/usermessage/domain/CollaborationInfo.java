package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class CollaborationInfo {

    /**
     * Conversation Identifier {@link String}
     */
    protected String conversationId;

    /**
     * Action information {@link String}
     */
    protected String action;

    /**
     * Agreement Reference {@link AgreementRef}
     */
    protected AgreementRef agreementRef;

    /**
     * Service details {@link Service}
     */
    protected Service service;

    /**
     * Gets Conversation Identifier
     * @return Conversation Identifier {@link String}
     */
    public String getConversationId() {
        return conversationId;
    }

    /**
     * Sets Conversation Identifier
     * @param conversationId Conversation Identifier {@link String}
     */
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Gets Action information
     * @return Action information {@link String}
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets Action information
     * @param action Action information {@link String}
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Gets Agreement Reference
     * @return Agreement Reference {@link AgreementRef}
     */
    public AgreementRef getAgreementRef() {
        return agreementRef;
    }

    /**
     * Sets Agreement Reference
     * @param agreementRef Agreement Reference {@link AgreementRef}
     */
    public void setAgreementRef(AgreementRef agreementRef) {
        this.agreementRef = agreementRef;
    }

    /**
     * Gets Service details
     * @return Service details {@link Service}
     */
    public Service getService() {
        return service;
    }

    /**
     * Sets Service details
     * @param service Service details {@link Service}
     */
    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CollaborationInfo that = (CollaborationInfo) o;

        return new EqualsBuilder()
                .append(conversationId, that.conversationId)
                .append(action, that.action)
                .append(agreementRef, that.agreementRef)
                .append(service, that.service)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(conversationId)
                .append(action)
                .append(agreementRef)
                .append(service)
                .toHashCode();
    }
}

package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class CollaborationInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Conversation Identifier {@link String}
     */
    private String conversationId;

    /**
     * Action information {@link String}
     */
    private String action;

    /**
     * Agreement Reference {@link AgreementRefDTO}
     */
    private AgreementRefDTO agreementRef;

    /**
     * Service details {@link ServiceDTO}
     */
    private ServiceDTO service;

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
     * @return Agreement Reference {@link AgreementRefDTO}
     */
    public AgreementRefDTO getAgreementRef() {
        return agreementRef;
    }

    /**
     * Sets Agreement Reference
     * @param agreementRef Agreement Reference {@link AgreementRefDTO}
     */
    public void setAgreementRef(AgreementRefDTO agreementRef) {
        this.agreementRef = agreementRef;
    }

    /**
     * Gets Service details
     * @return Service details {@link ServiceDTO}
     */
    public ServiceDTO getService() {
        return service;
    }

    /**
     * Sets Service details
     * @param service Service details {@link ServiceDTO}
     */
    public void setService(ServiceDTO service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("conversationId", conversationId)
                .append("action", action)
                .append("agreementRef", agreementRef)
                .append("service", service)
                .toString();
    }
}

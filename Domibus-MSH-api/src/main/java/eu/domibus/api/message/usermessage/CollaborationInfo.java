package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CollaborationInfo {

    private String conversationId;

    private String action;

    private AgreementRef agreementRef;

    private Service service;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public AgreementRef getAgreementRef() {
        return agreementRef;
    }

    public void setAgreementRef(AgreementRef agreementRef) {
        this.agreementRef = agreementRef;
    }

    public Service getService() {
        return service;
    }

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

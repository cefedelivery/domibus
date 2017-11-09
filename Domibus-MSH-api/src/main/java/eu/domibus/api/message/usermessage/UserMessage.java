package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class UserMessage {

    private MessageInfo messageInfo;

    private PartyInfo partyInfo;

    private CollaborationInfo collaborationInfo;

    private MessageProperties messageProperties;

    private PayloadInfo payloadInfo;

    private String mpc;

    public MessageInfo getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(MessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    public void setPartyInfo(PartyInfo partyInfo) {
        this.partyInfo = partyInfo;
    }

    public CollaborationInfo getCollaborationInfo() {
        return collaborationInfo;
    }

    public void setCollaborationInfo(CollaborationInfo collaborationInfo) {
        this.collaborationInfo = collaborationInfo;
    }

    public MessageProperties getMessageProperties() {
        return messageProperties;
    }

    public void setMessageProperties(MessageProperties messageProperties) {
        this.messageProperties = messageProperties;
    }

    public PayloadInfo getPayloadInfo() {
        return payloadInfo;
    }

    public void setPayloadInfo(PayloadInfo payloadInfo) {
        this.payloadInfo = payloadInfo;
    }

    public String getMpc() {
        return mpc;
    }

    public void setMpc(String mpc) {
        this.mpc = mpc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserMessage that = (UserMessage) o;

        return new EqualsBuilder()
                .append(messageInfo, that.messageInfo)
                .append(partyInfo, that.partyInfo)
                .append(collaborationInfo, that.collaborationInfo)
                .append(messageProperties, that.messageProperties)
                .append(payloadInfo, that.payloadInfo)
                .append(mpc, that.mpc)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageInfo)
                .append(partyInfo)
                .append(collaborationInfo)
                .append(messageProperties)
                .append(payloadInfo)
                .append(mpc)
                .toHashCode();
    }
}

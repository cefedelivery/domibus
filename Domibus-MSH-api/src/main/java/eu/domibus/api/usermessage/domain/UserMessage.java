package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class UserMessage {

    /**
     * User Message Info details {@link MessageInfo}
     */
    protected MessageInfo messageInfo;

    /**
     * User Message's Party Info details {@link PartyInfo}
     */
    protected PartyInfo partyInfo;

    /**
     * User Message's Collaboration Info details {@link CollaborationInfo}
     */
    protected CollaborationInfo collaborationInfo;

    /**
     * User Message's Properties Info details {@link MessageProperties}
     */
    protected MessageProperties messageProperties;

    /**
     * User Message's Payload Info details {@link PayloadInfo}
     */
    protected PayloadInfo payloadInfo;

    /**
     * User Message's Partition Channel {@link String}
     */
    protected String mpc;

    /**
     * Gets Message Info details
     * @return Message Info details {@link MessageInfo}
     */
    public MessageInfo getMessageInfo() {
        return messageInfo;
    }

    /**
     * Sets Message Info details
     * @param messageInfo Message Info details {@link MessageInfo}
     */
    public void setMessageInfo(MessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

    /**
     * Gets Party Info details
     * @return Party Info details {@link PartyInfo}
     */
    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    /**
     * Sets Party Info details
     * @param partyInfo Party Info details {@link PartyInfo}
     */
    public void setPartyInfo(PartyInfo partyInfo) {
        this.partyInfo = partyInfo;
    }

    /**
     * Gets Collaboration Info details
     * @return Collaboration Info details {@link CollaborationInfo}
     */
    public CollaborationInfo getCollaborationInfo() {
        return collaborationInfo;
    }

    /**
     * Sets Collaboration Info details
     * @param collaborationInfo Collaboration Info details {@link CollaborationInfo}
     */
    public void setCollaborationInfo(CollaborationInfo collaborationInfo) {
        this.collaborationInfo = collaborationInfo;
    }

    /**
     * Gets Message Properties details
     * @return Message Properties details {@link MessageProperties}
     */
    public MessageProperties getMessageProperties() {
        return messageProperties;
    }

    /**
     * Sets Message Properties details
     * @param messageProperties Message Properties details {@link MessageProperties}
     */
    public void setMessageProperties(MessageProperties messageProperties) {
        this.messageProperties = messageProperties;
    }

    /**
     * Gets Payload Info details
     * @return Payload Info details {@link PayloadInfo}
     */
    public PayloadInfo getPayloadInfo() {
        return payloadInfo;
    }

    /**
     * Sets Payload Info details
     * @param payloadInfo Paylo Info details {@link PayloadInfo}
     */
    public void setPayloadInfo(PayloadInfo payloadInfo) {
        this.payloadInfo = payloadInfo;
    }

    /**
     * Gets Message Partition Channel
     * @return Message Partition Channel {@link String}
     */
    public String getMpc() {
        return mpc;
    }

    /**
     * Sets Message Partition Channel
     * @param mpc Message Partition Channel {@link String}
     */
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

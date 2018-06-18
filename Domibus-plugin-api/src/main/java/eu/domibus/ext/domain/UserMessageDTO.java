package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class for the User Message
 *
 * It stores information about User messages
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class UserMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * User Message Info details {@link MessageInfoDTO}
     */
    private MessageInfoDTO messageInfo;

    /**
     * User Message's Party Info details {@link PartyInfoDTO}
     */
    private PartyInfoDTO partyInfo;

    /**
     * User Message's Collaboration Info details {@link CollaborationInfoDTO}
     */
    private CollaborationInfoDTO collaborationInfo;

    /**
     * User Message's Properties Info details {@link MessagePropertiesDTO}
     */
    private MessagePropertiesDTO messageProperties;

    /**
     * User Message's Payload Info details {@link PayloadInfoDTO}
     */
    private PayloadInfoDTO payloadInfo;

    /**
     * User Message's Partition Channel {@link String}
     */
    private String mpc;

    /**
     * Gets Message Info details
     * @return Message Info details {@link MessageInfoDTO}
     */
    public MessageInfoDTO getMessageInfo() {
        return messageInfo;
    }

    /**
     * Sets Message Info details
     * @param messageInfo Message Info details {@link MessageInfoDTO}
     */
    public void setMessageInfo(MessageInfoDTO messageInfo) {
        this.messageInfo = messageInfo;
    }

    /**
     * Gets Party Info details
     * @return Party Info details {@link PartyInfoDTO}
     */
    public PartyInfoDTO getPartyInfo() {
        return partyInfo;
    }

    /**
     * Sets Party Info details
     * @param partyInfo Party Info details {@link PartyInfoDTO}
     */
    public void setPartyInfo(PartyInfoDTO partyInfo) {
        this.partyInfo = partyInfo;
    }

    /**
     * Gets Collaboration Info details
     * @return Collaboration Info details {@link CollaborationInfoDTO}
     */
    public CollaborationInfoDTO getCollaborationInfo() {
        return collaborationInfo;
    }

    /**
     * Sets Collaboration Info details
     * @param collaborationInfo Collaboration Info details {@link CollaborationInfoDTO}
     */
    public void setCollaborationInfo(CollaborationInfoDTO collaborationInfo) {
        this.collaborationInfo = collaborationInfo;
    }

    /**
     * Gets Message Properties details
     * @return Message Properties details {@link MessagePropertiesDTO}
     */
    public MessagePropertiesDTO getMessageProperties() {
        return messageProperties;
    }

    /**
     * Sets Message Properties details
     * @param messageProperties Message Properties details {@link MessagePropertiesDTO}
     */
    public void setMessageProperties(MessagePropertiesDTO messageProperties) {
        this.messageProperties = messageProperties;
    }

    /**
     * Gets Payload Info details
     * @return Payload Info details {@link PayloadInfoDTO}
     */
    public PayloadInfoDTO getPayloadInfo() {
        return payloadInfo;
    }

    /**
     * Sets Payload Info details
     * @param payloadInfo Paylo Info details {@link PayloadInfoDTO}
     */
    public void setPayloadInfo(PayloadInfoDTO payloadInfo) {
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
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageInfo", messageInfo)
                .append("partyInfo", partyInfo)
                .append("collaborationInfo", collaborationInfo)
                .append("messageProperties", messageProperties)
                .append("payloadInfo", payloadInfo)
                .append("mpc", mpc)
                .toString();
    }
}

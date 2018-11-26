package eu.domibus.api.message.fragment;

import java.math.BigInteger;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public class MessageGroup {

    protected String groupId;

    protected BigInteger messageSize;

    protected Integer fragmentCount;

    protected String compressionAlgorithm;

    protected BigInteger compressedMessageSize;

    protected String soapAction;

    protected MessageHeader messageHeaderEntity;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public BigInteger getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(BigInteger messageSize) {
        this.messageSize = messageSize;
    }

    public Integer getFragmentCount() {
        return fragmentCount;
    }

    public void setFragmentCount(Integer fragmentCount) {
        this.fragmentCount = fragmentCount;
    }

    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(String compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }

    public BigInteger getCompressedMessageSize() {
        return compressedMessageSize;
    }

    public void setCompressedMessageSize(BigInteger compressedMessageSize) {
        this.compressedMessageSize = compressedMessageSize;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public MessageHeader getMessageHeaderEntity() {
        return messageHeaderEntity;
    }

    public void setMessageHeaderEntity(MessageHeader messageHeaderEntity) {
        this.messageHeaderEntity = messageHeaderEntity;
    }
}

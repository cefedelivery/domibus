package eu.domibus.core.message.fragment;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Entity
@Table(name = "TB_MESSAGE_GROUP")
@NamedQueries({
        @NamedQuery(name = "MessageGroupEntity.findByGroupId", query = "SELECT c FROM MessageGroupEntity c where c.groupId=:GROUP_ID")
})
public class MessageGroupEntity extends AbstractBaseEntity {

    @Column(name = "GROUP_ID")
    protected String groupId;

    @Column(name = "SOURCE_MESSAGE_ID")
    protected String sourceMessageId;

    @Column(name = "MESSAGE_SIZE")
    protected Long messageSize;

    @Column(name = "FRAGMENT_COUNT")
    protected Long fragmentCount;

    @Column(name = "COMPRESSION_ALGORITHM")
    protected String compressionAlgorithm;

    @Column(name = "COMPRESSED_MESSAGE_SIZE")
    protected Long compressedMessageSize;

    @Column(name = "SOAP_ACTION")
    protected String soapAction;

    @JoinColumn(name = "FK_MESSAGE_HEADER_ID")
    @OneToOne(cascade = CascadeType.ALL)
    protected MessageHeaderEntity messageHeaderEntity;

    @Column(name = "REJECTED")
    protected Boolean rejected;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Long getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(Long messageSize) {
        this.messageSize = messageSize;
    }

    public Long getFragmentCount() {
        return fragmentCount;
    }

    public void setFragmentCount(Long fragmentCount) {
        this.fragmentCount = fragmentCount;
    }

    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(String compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }

    public Long getCompressedMessageSize() {
        return compressedMessageSize;
    }

    public void setCompressedMessageSize(Long compressedMessageSize) {
        this.compressedMessageSize = compressedMessageSize;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public MessageHeaderEntity getMessageHeaderEntity() {
        return messageHeaderEntity;
    }

    public void setMessageHeaderEntity(MessageHeaderEntity messageHeaderEntity) {
        this.messageHeaderEntity = messageHeaderEntity;
    }

    public String getSourceMessageId() {
        return sourceMessageId;
    }

    public void setSourceMessageId(String sourceMessageId) {
        this.sourceMessageId = sourceMessageId;
    }

    public Boolean getRejected() {
        return rejected;
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }
}

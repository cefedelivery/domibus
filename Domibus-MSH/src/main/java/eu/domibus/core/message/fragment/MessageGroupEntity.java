package eu.domibus.core.message.fragment;

import eu.domibus.common.MSHRole;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * Entity class for storing message fragment group details. For more details about relations to other entities please check the SplitAndJoin specs.
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Entity
@Table(name = "TB_MESSAGE_GROUP")
@NamedQueries({
        @NamedQuery(name = "MessageGroupEntity.findByGroupId", query = "SELECT c FROM MessageGroupEntity c where c.groupId=:GROUP_ID"),
        @NamedQuery(name = "MessageGroupEntity.findReceivedNonExpiredOrRejected", query = "SELECT c FROM MessageGroupEntity c where c.mshRole = 'RECEIVING' " +
                "and ( (c.rejected is null or c.rejected=false) or (c.expired is null or c.expired=false) )")
})
public class MessageGroupEntity extends AbstractBaseEntity {

    @Column(name = "GROUP_ID")
    protected String groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "MSH_ROLE")
    private MSHRole mshRole;

    @Column(name = "SOURCE_MESSAGE_ID")
    protected String sourceMessageId;

    @Column(name = "MESSAGE_SIZE")
    protected BigInteger messageSize;

    @Column(name = "FRAGMENT_COUNT")
    protected Long fragmentCount;

    @Column(name = "RECEIVED_FRAGMENTS")
    protected Long receivedFragments = 0L;

    @Column(name = "COMPRESSION_ALGORITHM")
    protected String compressionAlgorithm;

    @Column(name = "COMPRESSED_MESSAGE_SIZE")
    protected BigInteger compressedMessageSize;

    @Column(name = "SOAP_ACTION")
    protected String soapAction;

    @JoinColumn(name = "FK_MESSAGE_HEADER_ID")
    @OneToOne(cascade = CascadeType.ALL)
    protected MessageHeaderEntity messageHeaderEntity;

    @Column(name = "REJECTED")
    protected Boolean rejected;

    @Column(name = "EXPIRED")
    protected Boolean expired;

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
        return BooleanUtils.toBoolean(rejected);
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Long getReceivedFragments() {
        return receivedFragments;
    }

    public void setReceivedFragments(Long receivedFragments) {
        this.receivedFragments = receivedFragments;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public synchronized void incrementReceivedFragments() {
        if (receivedFragments == null) {
            receivedFragments = 0L;
        }
        receivedFragments++;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupId", groupId)
                .append("mshRole", mshRole)
                .append("sourceMessageId", sourceMessageId)
                .append("messageSize", messageSize)
                .append("fragmentCount", fragmentCount)
                .append("receivedFragments", receivedFragments)
                .append("compressionAlgorithm", compressionAlgorithm)
                .append("compressedMessageSize", compressedMessageSize)
                .append("soapAction", soapAction)
                .append("messageHeaderEntity", messageHeaderEntity)
                .append("rejected", rejected)
                .append("expired", expired)
                .toString();
    }
}

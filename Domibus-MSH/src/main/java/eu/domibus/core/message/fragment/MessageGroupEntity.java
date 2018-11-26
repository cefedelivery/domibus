package eu.domibus.core.message.fragment;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.math.BigInteger;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public class MessageGroupEntity extends AbstractBaseEntity {

    @Column(name = "GROUP_ID")
    protected Integer groupId;

    @Column(name = "MESSAGE_SIZE")
    protected BigInteger messageSize;

    @Column(name = "FRAGMENT_COUNT")
    protected Integer fragmentCount;

    @Column(name = "COMPRESSION_ALGORITHM")
    protected String compressionAlgorithm;

    @Column(name = "COMPRESSED_MESSAGE_SIZE")
    protected BigInteger compressedMessageSize;

    @Column(name = "SOAP_ACTION")
    protected String soapAction;

    @JoinColumn(name = "MESSAGE_HEADER_ID")
    @OneToOne(cascade = CascadeType.ALL)
    protected MessageHeaderEntity messageHeaderEntity;

}

package eu.domibus.api.message.ebms3.model;

import org.w3c.dom.Element;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The OPTIONAL element is named
 * after a type of Signal message. It contains all header information for the Signal message. If this
 * element is not present, an element describing a User message MUST be present. Three types of
 * Signal messages are specified in this document: Pull signal (eb:PullRequest), Error signal
 * (eb:Error) and Receipt signal (eb:Receipt).
 * An ebMS signal does not require any SOAP Body: if the SOAP Body is not empty, it MUST be ignored by
 * the MSH, as far as interpretation of the signal is concerned.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignalMessage", propOrder = {"messageInfo", "pullRequest", "receipt", "error", "any"})
@Entity
@Table(name = "TB_SIGNAL_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "SignalMessage.findSignalMessageIdByRefMessageId",
                query = "select signalMessage.messageInfo.messageId from SignalMessage signalMessage where signalMessage.messageInfo.refToMessageId = :ORI_MESSAGE_ID"),
        @NamedQuery(name = "SignalMessage.findSignalMessageByRefMessageId",
                query = "select signalMessage from SignalMessage signalMessage where signalMessage.messageInfo.refToMessageId = :ORI_MESSAGE_ID"),
})
public class SignalMessage extends AbstractBaseEntity {

    @XmlElement(name = "MessageInfo", required = true)
    @OneToOne(cascade = CascadeType.ALL)
    protected MessageInfo messageInfo;
    @XmlElement(name = "PullRequest")
    @Embedded
    protected PullRequest pullRequest;
    @XmlElement(name = "Receipt")
    @OneToOne(cascade = CascadeType.ALL)
    protected Receipt receipt;
    @XmlElement(name = "Error")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "SIGNALMESSAGE_ID")
    protected Set<Error> error;
    @XmlAnyElement(lax = true)
    @Transient
    //According to how we read the spec those attributes serve no purpose in the AS4 profile, therefore they are discarded
    protected List<Object> any;

    /**
     * Gets the value of the messageInfo property.
     *
     * @return possible object is {@link MessageInfo }
     */
    public MessageInfo getMessageInfo() {
        return this.messageInfo;
    }

    /**
     * Sets the value of the messageInfo property.
     *
     * @param value allowed object is {@link MessageInfo }
     */
    public void setMessageInfo(final MessageInfo value) {
        this.messageInfo = value;
    }

    /**
     * This OPTIONAL attribute identifies the Message Partition Channel from which the message is to
     * be pulled. The absence of this attribute indicates the default MPC.
     *
     * @return possible object is {@link PullRequest }
     */
    public PullRequest getPullRequest() {
        return this.pullRequest;
    }

    /**
     * This OPTIONAL attribute identifies the Message Partition Channel from which the message is to
     * be pulled. The absence of this attribute indicates the default MPC.
     *
     * @param value allowed object is {@link PullRequest }
     */
    public void setPullRequest(final PullRequest value) {
        this.pullRequest = value;
    }

    /**
     * The eb:Receipt element MAY occur zero or one times; and, if present, SHOULD contain a single
     * ebbpsig:NonRepudiationInformation child element, as defined in the ebBP Signal Schema [ebBP-SIG].
     * The value of eb:MessageInfo/eb:RefToMessageId MUST refer to the message for which this signal is a
     * receipt.
     *
     * @return possible object is {@link Receipt }
     */
    public Receipt getReceipt() {
        return this.receipt;
    }

    /**
     * The eb:Receipt element MAY occur zero or one times; and, if present, SHOULD contain a single
     * ebbpsig:NonRepudiationInformation child element, as defined in the ebBP Signal Schema [ebBP-SIG].
     * The value of eb:MessageInfo/eb:RefToMessageId MUST refer to the message for which this signal is a
     * receipt.
     *
     * @param value allowed object is {@link Receipt }
     */
    public void setReceipt(final Receipt value) {
        this.receipt = value;
    }

    /**
     * The eb:Error element MAY occur zero or more times.
     *
     * @see eu.domibus.api.message.ebms3.model.Error
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the error property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getError().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link eu.domibus.api.message.ebms3.model.Error }
     */
    public Set<Error> getError() {
        if (this.error == null) {
            this.error = new HashSet<>();
        }
        return this.error;
    }

    /**
     * Gets the value of the any property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the any property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list null null     {@link Object }
     * {@link Element }
     */
    public List<Object> getAny() {
        if (this.any == null) {
            this.any = new ArrayList<>();
        }
        return this.any;
    }
}

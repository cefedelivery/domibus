package eu.domibus.ebms3.common.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.*;

/**
 * AgreementRef is a string value that identifies the agreement that governs the exchange. The P-Mode
 * under which the MSH operates for this message should be aligned with this agreement.
 * The value of an AgreementRef element MUST be unique within a namespace mutually agreed by the two
 * parties. This could be a concatenation of the From and To PartyId values, a URI containing the Internet
 * domain name of one of the parties, or a namespace offered and managed by some other naming or
 * registry service. It is RECOMMENDED that the AgreementRef be a URI. The AgreementRef MAY
 * reference an instance of a CPA as defined in [ebCPPA].
 * <p>An example of the AgreementRef element follows:
 * &lt;eb:AgreementRef&gt;http://registry.example.com/cpas/our_cpa.xml&lt;/eb:AgreementRef&gt;
 * <p>If a CPA is referred to and a Receiving MSH detects an inconsistency, then it MUST report it with an
 * "ValueInconsistent" error of severity "error". If the AgreementRef is not recognized, then the
 * Receiving MSH MUST report it as a "ValueNotRecognized" error of severity "error".
 *
 * @author Christian Koch
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgreementRef", propOrder = "value")
@Embeddable
public class AgreementRef {

    @XmlValue
    @Column(name = "AGREEMENT_REF_VALUE")
    protected String value;
    @XmlAttribute(name = "type")
    @Column(name = "AGREEMENT_REF_TYPE")
    protected String type;
    @XmlAttribute(name = "pmode")
    @Column(name = "AGREEMENT_REF_PMODE")
    protected String pmode;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * This OPTIONAL attribute indicates how the parties sending and receiving the message
     * will interpret the value of the reference (e.g. the value could be "ebcppa2.1" for parties using a
     * CPA-based agreement representation). There is no restriction on the value of the type attribute;
     * this choice is left to profiles of this specification. If the type attribute is not present, the content of
     * the eb:AgreementRef element MUST be a URI. If it is not a URI, then the MSH MUST report a
     * "ValueInconsistent" error of severity "error".
     *
     * @return possible object is {@link String }
     */
    public String getType() {
        return this.type;
    }

    /**
     * This OPTIONAL attribute indicates how the parties sending and receiving the message
     * will interpret the value of the reference (e.g. the value could be "ebcppa2.1" for parties using a
     * CPA-based agreement representation). There is no restriction on the value of the type attribute;
     * this choice is left to profiles of this specification. If the type attribute is not present, the content of
     * the eb:AgreementRef element MUST be a URI. If it is not a URI, then the MSH MUST report a
     * "ValueInconsistent" error of severity "error".
     *
     * @param value allowed object is {@link String }
     */
    public void setType(final String value) {
        this.type = value;
    }

    /**
     * This OPTIONAL attribute allows for explicit association of a message with a P-Mode.
     * When used, its value contains the PMode.ID parameter.
     *
     * @return possible object is {@link String }
     */
    public String getPmode() {
        return this.pmode;
    }

    /**
     * This OPTIONAL attribute allows for explicit association of a message with a P-Mode.
     * When used, its value contains the PMode.ID parameter.
     *
     * @param value allowed object is {@link String }
     */
    public void setPmode(final String value) {
        this.pmode = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AgreementRef)) return false;

        final AgreementRef that = (AgreementRef) o;

        if (this.pmode != null ? !this.pmode.equalsIgnoreCase(that.pmode) : that.pmode != null) return false;
        if (this.type != null ? !this.type.equalsIgnoreCase(that.type) : that.type != null) return false;
        return !(this.value != null ? !this.value.equalsIgnoreCase(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = this.value != null ? this.value.hashCode() : 0;
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        result = 31 * result + (this.pmode != null ? this.pmode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("type", type)
                .append("pmode", pmode)
                .toString();
    }
}

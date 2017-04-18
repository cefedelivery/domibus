package eu.domibus.common.model.configuration;

import eu.domibus.common.xmladapter.ReplyPatternAdapter;
import eu.domibus.api.message.ebms3.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="replyPattern" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
@Table(name = "TB_RELIABILITY")
public class Reliability extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;
    @XmlAttribute(name = "replyPattern", required = true)
    @XmlJavaTypeAdapter(ReplyPatternAdapter.class)
    @Column(name = "REPLY_PATTERN")
    @Enumerated(EnumType.STRING)
    protected ReplyPattern replyPattern;
    @XmlAttribute(name = "nonRepudiation", required = true)
    @Column(name = "NON_REPUDIATION")
    protected boolean nonRepudiation;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Reliability)) return false;
        if (!super.equals(o)) return false;

        final Reliability that = (Reliability) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the replyPattern property.
     *
     * @return possible object is
     * {@link String }
     */
    public ReplyPattern getReplyPattern() {
        return this.replyPattern;
    }

    /**
     * Sets the value of the replyPattern property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReplyPattern(final ReplyPattern value) {
        this.replyPattern = value;
    }

    public boolean isNonRepudiation() {
        return this.nonRepudiation;
    }

    public void setNonRepudiation(final boolean nonRepudiation) {
        this.nonRepudiation = nonRepudiation;
    }

    public void init(final Configuration configuration) {

    }
}

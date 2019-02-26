package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Auto-generate JAXB class used for splitting configuration in the PMode configuration management. This classes has been modified to serve also an entity class.
 *
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="name" use="required" type="{http://domibus.eu/configuration}max255-non-empty-string" /&gt;
 *       &lt;attribute name="fragmentSize" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="compression" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="compressionAlgorithm" type="{http://domibus.eu/configuration}max255-non-empty-string" /&gt;
 *       &lt;attribute name="joinInterval" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
@Table(name = "TB_SPLITTING")
@NamedQueries({
        @NamedQuery(name = "Splitting.findByName",
                query = "select splitting from Splitting splitting where splitting.name=:NAME")})

public class Splitting extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;

    @XmlAttribute(name = "fragmentSize", required = true)
    @Column(name = "FRAGMENT_SIZE")
    protected int fragmentSize;

    @XmlAttribute(name = "compression", required = true)
    @Column(name = "COMPRESSION")
    protected boolean compression;

    @XmlAttribute(name = "compressionAlgorithm")
    @Column(name = "COMPRESSION_ALGORITHM")
    protected String compressionAlgorithm;

    @XmlAttribute(name = "joinInterval", required = true)
    @Column(name = "JOIN_INTERVAL")
    protected int joinInterval;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Splitting splitting = (Splitting) o;

        return new EqualsBuilder()
                .append(name, splitting.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("fragmentSize", fragmentSize)
                .append("compression", compression)
                .append("compressionAlgorithm", compressionAlgorithm)
                .append("joinInterval", joinInterval)
                .toString();
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the fragmentSize property.
     *
     * @return possible object is
     * {@link int }
     */
    public int getFragmentSize() {
        return fragmentSize;
    }

    /**
     * Sets the value of the fragmentSize property.
     *
     * @param value allowed object is
     *              {@link int }
     */
    public void setFragmentSize(int value) {
        this.fragmentSize = value;
    }

    /**
     * Gets the value of the compression property.
     *
     * @return possible object is
     * {@link String }
     */
    public boolean getCompression() {
        return compression;
    }

    /**
     * Sets the value of the compression property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCompression(boolean value) {
        this.compression = value;
    }

    /**
     * Gets the value of the compressionAlgorithm property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    /**
     * Sets the value of the compressionAlgorithm property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCompressionAlgorithm(String value) {
        this.compressionAlgorithm = value;
    }

    /**
     * Gets the value of the joinInterval property.
     *
     * @return possible object is
     * {@link int }
     */
    public int getJoinInterval() {
        return joinInterval;
    }

    /**
     * Sets the value of the joinInterval property.
     *
     * @param value allowed object is
     *              {@link int }
     */
    public void setJoinInterval(int value) {
        this.joinInterval = value;
    }
}
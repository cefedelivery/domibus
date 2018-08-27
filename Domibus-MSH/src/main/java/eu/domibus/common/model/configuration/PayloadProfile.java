package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = "attachment")
@Entity
@Table(name = "TB_PAYLOAD_PROFILE")
public class PayloadProfile extends AbstractBaseEntity {

    @XmlElement(required = true, name = "attachment")
    @Transient
    protected List<Attachment> attachment; //NOSONAR
    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;
    @XmlAttribute(name = "maxSize", required = true)
    @Column(name = "MAX_SIZE")
    protected int maxSize;
    @XmlTransient
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "TB_JOIN_PAYLOAD_PROFILE", joinColumns = @JoinColumn(name = "FK_PAYLOAD"), inverseJoinColumns = @JoinColumn(name = "FK_PROFILE"))
    private Set<Payload> payloads;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PayloadProfile)) return false;
        if (!super.equals(o)) return false;

        final PayloadProfile that = (PayloadProfile) o;

        if (!name.equalsIgnoreCase(that.name)) return false;

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
     * Gets the value of the maxSize property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public int getMaxSize() {
        return this.maxSize;
    }

    /**
     * Sets the value of the maxSize property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setMaxSize(final int value) {
        this.maxSize = value;
    }

    void init(final Configuration configuration) {
        this.payloads = new HashSet<>();
        for (final Attachment att : this.attachment) {
            for (final Payload payload : configuration.getBusinessProcesses().getPayloads()) {
                if (payload.getName().equalsIgnoreCase(att.getName())) {
                    this.payloads.add(payload);
                    break;
                }
            }
        }
    }

    public Set<Payload> getPayloads() {
        return this.payloads;
    }
}

package eu.domibus.common.model.configuration;

import eu.domibus.api.message.ebms3.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;


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
 *       &lt;attribute name="retry" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="duplicateDetection" type="{http://www.w3.org/2001/XMLSchema}string" />
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
@Table(name = "TB_RECEPTION_AWARENESS")
public class ReceptionAwareness extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;

    @XmlAttribute(name = "retry")
    @Transient
    protected String retryXml;

    @XmlTransient
    @Column(name = "RETRY_TIMEOUT")
    protected int retryTimeout;

    @XmlTransient
    @Column(name = "RETRY_COUNT")
    protected int retryCount;

    @XmlTransient
    @Column(name = "STRATEGY")
    @Enumerated(EnumType.STRING)
    protected RetryStrategy strategy = RetryStrategy.SEND_ONCE;

    @XmlAttribute(name = "duplicateDetection")
    @Column(name = "DUPLICATE_DETECTION")
    protected boolean duplicateDetection;

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
     * Gets the value of the duplicateDetection property.
     *
     * @return possible object is
     * {@link String }
     */
    public boolean getDuplicateDetection() {
        return this.duplicateDetection;
    }

    /**
     * Sets the value of the duplicateDetection property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDuplicateDetection(final boolean value) {
        this.duplicateDetection = value;
    }

    public RetryStrategy getStrategy() {
        return this.strategy;
    }

    public void setStrategy(final RetryStrategy strategy) {
        this.strategy = strategy;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void setRetryCount(final int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryTimeout() {
        return this.retryTimeout;
    }

    public void setRetryTimeout(final int retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceptionAwareness)) return false;
        if (!super.equals(o)) return false;

        final ReceptionAwareness that = (ReceptionAwareness) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public void init(final Configuration configuration) {
        if (this.retryXml != null) {
            final String[] retryValues = this.retryXml.split(";");
            this.retryTimeout = Integer.parseInt(retryValues[0]);
            this.retryCount = Integer.parseInt(retryValues[1]);
            this.strategy = RetryStrategy.valueOf(retryValues[2]);
        }

    }
}

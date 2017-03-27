package eu.domibus.core.acknowledge;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Entity
@Table(name = "TB_MSG_ACKNOWLEDGE_PROP")
public class MessageAcknowledgementProperty extends AbstractBaseEntity {

    @Column(name = "PROPERTY_NAME")
    private String name;

    @Column(name = "PROPERTY_VALUE")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAcknowledgementProperty messageAcknowledgementProperty = (MessageAcknowledgementProperty) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(name, messageAcknowledgementProperty.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37)
                .appendSuper(super.hashCode())
                .append(name)
                .toHashCode();
    }
}

package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class PartyId {

    /**
     * Party Identifier {@link String}
     */
    protected String value;

    /**
     * Party Type {@link String}
     */
    protected String type;

    /**
     * Gets the Party Identifier
     * @return Party Identifier {@link String}
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the Party Identifier
     * @param value Party Identifier {@link String}
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the Party Type
     * @return Party Type {@link String}
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the Party Type
     * @param type Party Type {@link String}
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartyId partyId = (PartyId) o;

        return new EqualsBuilder()
                .append(value, partyId.value)
                .append(type, partyId.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(type)
                .toHashCode();
    }
}

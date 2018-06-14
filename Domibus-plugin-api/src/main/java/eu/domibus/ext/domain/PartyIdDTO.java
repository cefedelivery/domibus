package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class that stores Party Identifier information
 *
 * It stores information about Party Identifier details
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class PartyIdDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Party Identifier {@link String}
     */
    private String value;

    /**
     * Party Type {@link String}
     */
    private String type;

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
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("type", type)
                .toString();
    }
}

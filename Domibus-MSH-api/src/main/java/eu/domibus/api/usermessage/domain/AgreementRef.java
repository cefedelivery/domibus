package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class AgreementRef {

    /**
     * Agreement Reference Value {@link String}
     */
    protected String value;

    /**
     * Agreement Reference Type {@link String}
     */
    protected String type;

    /**
     * Agreement Reference PMode {@link String}
     */
    protected String pmode;

    /**
     * Gets the Agreement Reference Value
     * @return Agreement Reference Value {@link String}
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the Agreement Reference Value
     * @param value Agreement Reference Value {@link String}
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the Agreement Reference Type
     * @return Agreement Reference Type {@link String}
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the Agreement Reference Type
     * @param type Agreement Reference Type {@link String}
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets The Agreement Reference PMode
     * @return Agreement Reference PMode {@link String}
     */
    public String getPmode() {
        return pmode;
    }

    /**
     * Sets the Agreement Reference PMode
     * @param pmode Agreement Reference PMode {@link String}
     */
    public void setPmode(String pmode) {
        this.pmode = pmode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AgreementRef that = (AgreementRef) o;

        return new EqualsBuilder()
                .append(value, that.value)
                .append(type, that.type)
                .append(pmode, that.pmode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(type)
                .append(pmode)
                .toHashCode();
    }
}

package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class for Agreement Reference information details
 *
 * It stores information about Agreement Reference details
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class AgreementRefDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Agreement Reference Value {@link String}
     */
    private String value;

    /**
     * Agreement Reference Type {@link String}
     */
    private String type;

    /**
     * Agreement Reference PMode {@link String}
     */
    private String pmode;

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
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("type", type)
                .append("pmode", pmode)
                .toString();
    }
}

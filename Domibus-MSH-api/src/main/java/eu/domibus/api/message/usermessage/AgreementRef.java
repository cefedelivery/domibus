package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class AgreementRef {

    protected String value;

    protected String type;

    protected String pmode;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPmode() {
        return pmode;
    }

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

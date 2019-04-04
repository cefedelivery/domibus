package eu.domibus.core.crypto.spi;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Just a mapper class for core Domain class.
 */
public class DomainSpi {

    protected String code;
    protected String name;

    public DomainSpi() {
    }

    public DomainSpi(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DomainSpi domain = (DomainSpi) o;

        return new EqualsBuilder()
                .append(code, domain.code)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(code)
                .toHashCode();
    }

    @Override
    public String toString() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

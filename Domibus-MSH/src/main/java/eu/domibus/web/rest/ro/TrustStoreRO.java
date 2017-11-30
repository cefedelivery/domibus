package eu.domibus.web.rest.ro;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Date;
import java.util.Objects;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class TrustStoreRO {

    private String name;
    private String subject;
    private String issuer;
    private Date validFrom;
    private Date validUntil;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String toCsvString() {
        // RFC 4180
        return new StringBuilder()
                .append(Objects.toString(name,"")).append(",")
                .append("\"").append(Objects.toString(subject,"")).append("\"").append(",")
                .append("\"").append(Objects.toString(issuer,"")).append("\"").append(",")
                .append(Objects.toString(validFrom,"")).append(",")
                .append(Objects.toString(validUntil,""))
                .append(System.lineSeparator())
                .toString();
    }

    public static String csvTitle() {
        return new StringBuilder()
                .append("Name").append(",")
                .append("Subject").append(",")
                .append("Issuer").append(",")
                .append("Valid From").append(",")
                .append("Valid Until")
                .append(System.lineSeparator())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TrustStoreRO that = (TrustStoreRO) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(subject, that.subject)
                .append(issuer, that.issuer)
                .append(validFrom, that.validFrom)
                .append(validUntil, that.validUntil)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(subject)
                .append(issuer)
                .append(validFrom)
                .append(validUntil)
                .toHashCode();
    }
}

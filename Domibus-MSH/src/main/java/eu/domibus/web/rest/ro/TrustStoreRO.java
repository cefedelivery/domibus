package eu.domibus.web.rest.ro;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

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
    private String fingerprints;

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

    public String getFingerprints() {
        return fingerprints;
    }

    public void setFingerprints(String fingerprints) {
        this.fingerprints = fingerprints;
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
                .append(fingerprints, that.fingerprints)
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
                .append(fingerprints)
                .toHashCode();
    }
}

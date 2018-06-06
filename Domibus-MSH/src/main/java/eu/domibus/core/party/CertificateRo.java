package eu.domibus.core.party;

import java.util.Date;
import java.util.Objects;

/**
 * @author pion
 * @since 4.0
 */
public class CertificateRo {

    private String subjectName;
    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    private Date validityFrom;
    public Date getValidityFrom() {
        return validityFrom;
    }
    public void setValidityFrom(Date validityFrom) {
        this.validityFrom = validityFrom;
    }

    private Date validityTo;
    public Date getValidityTo() {
        return validityTo;
    }
    public void setValidityTo(Date validityTo) {
        this.validityTo = validityTo;
    }

    private String issuer;
    public String getIssuer() {
        return issuer;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    private String fingerprints;
    public String getFingerprints() {
        return fingerprints;
    }
    public void setFingerprints(String issuer) {
        this.fingerprints = fingerprints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CertificateRo obj = (CertificateRo) o;

        if (!Objects.equals(subjectName, obj.subjectName)) return false;
        if (!Objects.equals(validityFrom, obj.validityFrom)) return false;
        if (!Objects.equals(validityTo, obj.validityTo)) return false;
        if (!Objects.equals(issuer, obj.issuer)) return false;

        return Objects.equals(fingerprints, obj.fingerprints);
    }

}

package eu.domibus.api.security;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class TrustStoreEntry {

    private String name;
    private String subject;
    private String issuer;
    private Date validFrom;
    private Date validUntil;

    public TrustStoreEntry(String name, String subject, String issuer, Date validFrom, Date validUntil) {
        this.name = name;
        this.subject = subject;
        this.issuer = issuer;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }


    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }
}

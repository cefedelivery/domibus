package eu.domibus.api.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.*;

public class X509CertificateAuthentication implements Authentication {

    private boolean authenticated;
    private String certificateId;
    private String originalUser = null;
    private X509Certificate[] certificates;
    private Collection<GrantedAuthority> authorityList;

    public X509CertificateAuthentication(final X509Certificate[] certificates) {
        this.certificates = certificates;
        this.certificateId = calculateCertificateId(getCertificate(certificates));
    }

    public String getCertificateId() {
        return certificateId;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorityList;
    }

    public void setAuthorityList(Collection<GrantedAuthority> authorityList) {
        this.authorityList = authorityList;
    }

    @Override
    public Object getCredentials() {
        return certificates;
    }

    @Override
    public Object getDetails() {
        return certificateId;
    }

    @Override
    public Object getPrincipal() {
        return originalUser;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return certificateId;
    }

    protected X509Certificate getCertificate(final X509Certificate[] requestCerts) {
        if (requestCerts == null || requestCerts.length == 0) {
            // Empty array
            return null;
        }

        /* In our self signed certificates issuer equals subject (C=BE,O=eDelivery,CN=instanceA) */
        if (requestCerts.length == 1) {
            return requestCerts[0];
        }

        // Find all certificates that are not issuer to another certificate
        final List<X509Certificate> nonIssuerCertList = new ArrayList<X509Certificate>();
        for (final X509Certificate requestCert : requestCerts) {
            final X500Principal subject = requestCert.getSubjectX500Principal();

            // Search for the issuer of the current certificate
            boolean found = false;
            for (final X509Certificate issuerCert : requestCerts)
                if (subject.equals(issuerCert.getIssuerX500Principal())) {
                    found = true;
                    break;
                }
            if (!found)
                nonIssuerCertList.add(requestCert);
        }

        // Do we have exactly 1 certificate to verify?
        if (nonIssuerCertList.size() != 1)
            throw new AuthenticationException(("Found " +
                    nonIssuerCertList.size() +
                    " certificates that are not issuer certificates!"));

        final X509Certificate nonIssuerCert = nonIssuerCertList.get(0);
        return nonIssuerCert;
    }

    protected String calculateCertificateId(final X509Certificate cert) {
        if(cert == null) {
            throw new IllegalArgumentException("Certificate is null");
        }
        // subject principal name must be in the order CN=XX,O=YY,C=ZZ
        // In some JDK versions it is O=YY,CN=XX,C=ZZ instead (e.g. 1.6.0_45)
        try {
            final LdapName ldapName = new LdapName(cert.getSubjectX500Principal().getName());

            // Make a map from type to name
            final Map<String, Rdn> parts = new HashMap<>();
            for (final Rdn rdn : ldapName.getRdns()) {
                parts.put(rdn.getType(), rdn);
            }

            // Re-order - least important item comes first (=reverse order)!
            final String subjectName = parts.get("CN").toString() + "," + parts.get("O").toString() + "," + parts.get("C").toString();

            // subject-name + ":" + serial number hexstring
            String serialNumber = StringUtils.leftPad(cert.getSerialNumber().toString(), 16, "0");
            return subjectName + ':' + serialNumber;
        } catch (final Exception exc) {
            throw new AuthenticationException("Impossible to calculate the certificate Id of certificate " + cert.getSubjectX500Principal(), exc);
        }
    }

}

package eu.domibus.api.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Blue Coat is the name of the reverse proxy at the commission. It forwards the request in HTTP with the certificate details inside the request. This class extracts the data from the header.
 * Created by feriaad on 17/06/2015.
 */
public class BlueCoatClientCertificateAuthentication implements Authentication {

    private static final Locale LOCALE = Locale.US;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private boolean authenticated;
    private String certificateId;
    private String originalUser = null;
    private CertificateDetails certificate;
    private Collection<GrantedAuthority> authorityList;

    private static String HEADER_ATTRIBUTE_SEPARATOR = "&";
    private static String[] HEADER_ATTRIBUTE_SUBJECT = {"subject"};
    private static String[] HEADER_ATTRIBUTE_SERIAL = {"serial", "sno"};
    private static String[] HEADER_ATTRIBUTE_VALID_FROM = {"validFrom"};
    private static String[] HEADER_ATTRIBUTE_VALID_TO = {"validTo"};
    private static String[] HEADER_ATTRIBUTE_ISSUER = {"issuer"};

    public BlueCoatClientCertificateAuthentication(final String certHeaderValue) {
        certificate = new CertificateDetails();
        this.certificateId = calculateCertificateId(certHeaderValue);
    }

    public String getCertificateId() {
        return certificateId;
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorityList;
    }

    public void setAuthorityList(Collection<GrantedAuthority> authorityList) {
        this.authorityList = authorityList;
    }

    @Override
    public Object getCredentials() {
        return certificate;
    }

    @Override
    public Object getDetails() {
        return certificate;
    }

    @Override
    public Object getPrincipal() {
        return originalUser;
    }

    @Override
    public String getName() {
        return certificateId;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    protected String calculateCertificateId(final String certHeaderValue) throws AuthenticationException {
        try {
            String clientCertHeaderDecoded = URLDecoder.decode(certHeaderValue, DEFAULT_CHARSET.name());
            parseClientCertHeader(clientCertHeaderDecoded);
            certificate.setSerial(certificate.getSerial().replaceAll(":", ""));
            String subject = certificate.getSubject();

            LdapName ldapName;
            try {
                ldapName = new LdapName(subject);
            } catch (InvalidNameException exc) {
                throw new AuthenticationException("Impossible to identify authorities for certificate " + subject, exc);
            }
            // Make a map from type to name
            final Map<String, Rdn> parts = new HashMap<>();
            for (final Rdn rdn : ldapName.getRdns()) {
                parts.put(rdn.getType(), rdn);
            }

            final String subjectName = parts.get("CN").toString() + "," + parts.get("O").toString() + "," + parts.get("C").toString();

            // subject-name + ":" + serial number hexstring
            String serialNumber = StringUtils.leftPad(certificate.getSerial(), 16, "0");
            certificate.setSubject(subjectName);
            return subjectName + ':' + serialNumber;
        } catch (final Exception exc) {
            throw new AuthenticationException("Impossible to determine the certificate identifier from " + certHeaderValue, exc);
        }
    }

    private void parseClientCertHeader(String clientCertHeaderDecoded) throws AuthenticationException {
        String[] split = clientCertHeaderDecoded.split(HEADER_ATTRIBUTE_SEPARATOR);

        if (split.length != 5) {
            throw new AuthenticationException(
                    "Invalid BlueCoat Client Certificate Header Received ");
        }
        DateFormat df = new SimpleDateFormat("MMM d hh:mm:ss yyyy zzz", LOCALE);
        for (final String attribute : split) {
            if (isIn(attribute, HEADER_ATTRIBUTE_ISSUER)) {
                certificate.setIssuer(attribute.substring(attribute.indexOf('=') + 1));
            } else if (isIn(attribute, HEADER_ATTRIBUTE_SERIAL)) {
                certificate.setSerial(attribute.substring(attribute.indexOf('=') + 1));
            } else if (isIn(attribute, HEADER_ATTRIBUTE_SUBJECT)) {
                certificate.setSubject(attribute.substring(attribute.indexOf('=') + 1));
            } else if (isIn(attribute, HEADER_ATTRIBUTE_VALID_FROM)) {
                try {
                    certificate.setValidFrom(DateUtils.toCalendar(df.parse(attribute.substring(attribute.indexOf('=') + 1))));
                } catch (ParseException e) {
                    throw new AuthenticationException(
                            "Invalid BlueCoat Client Certificate Header Received (Unparsable Date for " + HEADER_ATTRIBUTE_VALID_FROM + ") ");
                }
            } else if (isIn(attribute, HEADER_ATTRIBUTE_VALID_TO)) {
                try {
                    certificate.setValidTo(DateUtils.toCalendar(df.parse(attribute.substring(attribute.indexOf('=') + 1))));
                } catch (ParseException e) {
                    throw new AuthenticationException(
                            "Invalid BlueCoat Client Certificate Header Received (Unparsable Date for " + HEADER_ATTRIBUTE_VALID_TO + ") ");
                }
            } else {
                throw new AuthenticationException(
                        "Unknown BlueCoat Client Certificate Header Received: " + attribute);
            }
        }
        certificate.setRootCertificateDN(certificate.getIssuer());
    }

    private boolean isIn(String attribute, String[] headerAttributes) {
        for (String headerAttribute : headerAttributes) {
            if (attribute.toLowerCase(LOCALE).startsWith(headerAttribute.toLowerCase(LOCALE))) {
                return true;
            }
        }
        return false;
    }
}

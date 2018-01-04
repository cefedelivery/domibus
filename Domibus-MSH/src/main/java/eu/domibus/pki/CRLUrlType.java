package eu.domibus.pki;

import org.apache.commons.lang.StringUtils;

/**
 * Enum which contains supported CRL Url types
 *
 * @author Catalin Enache
 * @version 1.0
 * @since 03/01/2018
 */
public enum CRLUrlType {
    HTTP("http://"),
    HTTPS("https://"),
    FTP("ftp://"),
    FILE("file:/"),
    LDAP("ldap://");

    final String prefix;

    CRLUrlType(final String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns true if the given {@code crlURL} starts with the prefix defined
     *
     * @param crlURL url to check
     * @return boolean
     */
    public boolean canHandleURL(final String crlURL) {
        return !StringUtils.isBlank(crlURL) && crlURL.toLowerCase().startsWith(prefix);
    }

    /**
     * Check if the given url is among the ones declared in this enum
     *
     * @param crlURL url to be checked
     * @return boolean
     */
    public static boolean isURLSupported(final String crlURL) {
        if (StringUtils.isBlank(crlURL)) {
            return false;
        }
        for (CRLUrlType c : CRLUrlType.values()) {
            if (crlURL.toLowerCase().startsWith(c.getPrefix())) {
                return true;
            }
        }
        return false;
    }
}

package eu.domibus.ebms3.receiver;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public enum CertificateExchangeType {
    NONE,
    KEY_INFO,
    BINARY_SECURITY_TOKEN;

    public static String getKey() {
        return CertificateExchangeType.class.getSimpleName();
    }

    public static String getValue() {
        return CertificateExchangeType.class.getSimpleName() + "_value";
    }
}

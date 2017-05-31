package eu.domibus.common.util;

import java.security.cert.X509Certificate;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 3.2.5
 */
public class EndpointInfo {

    private String address;
    private X509Certificate certificate;

    public EndpointInfo(String address, X509Certificate certificate) {
        this.address = address;
        this.certificate = certificate;
    }

    public String getAddress() {
        return address;

    }

    public void setAddress(String address) {
        this.address = address;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }
}
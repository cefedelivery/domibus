package eu.domibus.plugin.webService.service;

import eu.domibus.plugin.webService.security.CertificateDetails;
import eu.domibus.plugin.webService.common.exception.AuthenticationException;

/**
 * Created by feriaad on 18/06/2015.
 */
public interface IBlueCoatCertificateService {
    /**
     * Validate a certificate sent by the BlueCoat server in the HTTP header
     *
     * @param certificate the certificate sent by the BlueCoat server in the HTTP header
     * @return true if the certificate is valid, false otherwise
     * @throws AuthenticationException an authentication exception
     */
    boolean isBlueCoatClientCertificateValid(final CertificateDetails certificate) throws AuthenticationException;
}

package eu.domibus.wss4j.common.crypto.api;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainCertificateProviderFactory {

    DomainCertificateProvider createDomainCertificateProvider(String domain);
}

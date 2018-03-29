package eu.domibus.wss4j.common.crypto.api;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DomainCertificateProviderFactory {

    DomainCertificateProvider createDomainCertificateProvider(String domain);
}

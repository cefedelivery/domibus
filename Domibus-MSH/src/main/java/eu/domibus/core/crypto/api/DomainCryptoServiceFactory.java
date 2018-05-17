package eu.domibus.core.crypto.api;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainCryptoServiceFactory {

    DomainCryptoService createDomainCryptoService(Domain domain);
}

package eu.domibus.wss4j.common.crypto.api;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainContextProvider {

    Domain getCurrentDomain();
}

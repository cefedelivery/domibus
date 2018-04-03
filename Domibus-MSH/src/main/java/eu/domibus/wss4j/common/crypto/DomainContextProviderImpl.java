package eu.domibus.wss4j.common.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.wss4j.common.crypto.api.DomainContextProvider;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainContextProviderImpl implements DomainContextProvider {

    @Override
    public Domain getCurrentDomain() {
        //TODO get the domain from MDC
        return DomainService.DEFAULT_DOMAIN;
    }
}

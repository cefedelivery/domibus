package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainServiceImpl implements DomainService {

    @Override
    public List<Domain> getDomains() {
        //TODO get the domains from the database/properties
        return Arrays.asList(DEFAULT_DOMAIN);
    }

    @Override
    public Domain getDomain(String code) {
        //TODO get the domains from the database/properties
        return DEFAULT_DOMAIN;
    }
}

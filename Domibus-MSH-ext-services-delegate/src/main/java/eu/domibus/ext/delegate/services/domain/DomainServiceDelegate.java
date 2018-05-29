package eu.domibus.ext.delegate.services.domain;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.ext.services.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class DomainServiceDelegate implements DomainService {

    @Autowired
    DomainContextProvider domainContextProvider;

    @Override
    public String getDomainName() {
        return domainContextProvider.getCurrentDomain().getName();
    }
}

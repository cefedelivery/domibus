package eu.domibus.ext.delegate.services.domain;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class DomainExtServiceDelegate implements DomainExtService {

    @Autowired
    DomainContextProvider domainContextProvider;

    @Autowired
    DomainService domainService;

    @Autowired
    DomainExtConverter domainConverter;

    @Override
    public DomainDTO getDomainForScheduler(String schedulerName) {
        return domainConverter.convert(domainService.getDomainForScheduler(schedulerName), DomainDTO.class);
    }

    public DomainDTO getDomain(String code) {
        return domainConverter.convert(domainService.getDomain(code), DomainDTO.class);
    }
}

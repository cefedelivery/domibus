package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomibusPropertyExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomibusPropertyServiceDelegate implements DomibusPropertyExtService {

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainExtConverter domainConverter;

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyProvider.getProperty(propertyName);
    }

    @Override
    public String getProperty(String propertyName, String defaultValue) {
        return domibusPropertyProvider.getProperty(propertyName, defaultValue);
    }

    @Override
    public String getDomainProperty(DomainDTO domainCode, String propertyName) {
        final Domain domain = domainConverter.convert(domainCode, Domain.class);
        return domibusPropertyProvider.getProperty(domain, propertyName);
    }

    @Override
    public String getDomainProperty(DomainDTO domainCode, String propertyName, String defaultValue) {
        final Domain domain = domainConverter.convert(domainCode, Domain.class);
        return domibusPropertyProvider.getProperty(domain, propertyName, defaultValue);
    }

    @Override
    public String getDomainResolvedProperty(DomainDTO domainCode, String propertyName) {
        final Domain domain = domainConverter.convert(domainCode, Domain.class);
        return domibusPropertyProvider.getResolvedProperty(domain, propertyName);
    }

    @Override
    public String getResolvedProperty(String propertyName) {
        return domibusPropertyProvider.getResolvedProperty(propertyName);
    }
}

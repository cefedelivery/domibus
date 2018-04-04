package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainContextProviderImpl implements DomainContextProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainContextProviderImpl.class);

    @Autowired
    DomainService domainService;

    @Override
    public Domain getCurrentDomain() {
        final String domainCode = LOG.getMDC(DomibusLogger.MDC_DOMAIN);
        if (StringUtils.isEmpty(domainCode)) {
            throw new DomainException("Could get current domain: domain is not set");
        }
        final Domain domain = domainService.getDomain(domainCode);
        if(domain == null) {
            throw new DomainException("Could get current domain: domain with code [{}] is not configured");
        }
        return domain;
    }

    @Override
    public void setCurrentDomain(String domainCode) {
        if (StringUtils.isEmpty(domainCode)) {
            throw new DomainException("Could not set current domain: domain is empty");
        }
        LOG.putMDC(DomibusLogger.MDC_DOMAIN, domainCode);
    }
}

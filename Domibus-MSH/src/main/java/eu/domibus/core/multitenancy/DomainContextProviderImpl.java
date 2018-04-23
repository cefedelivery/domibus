package eu.domibus.core.multitenancy;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DomainContextProviderImpl implements DomainContextProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainContextProviderImpl.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Override
    public Domain getCurrentDomain() {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOG.trace("No multi-tenancy aware: returning the default domain");
            return DomainService.DEFAULT_DOMAIN;
        }

        String domainCode = LOG.getMDC(DomibusLogger.MDC_DOMAIN);
        if (StringUtils.isEmpty(domainCode)) {
            throw new DomainException("Could not get current domain");
        }
        final Domain domain = domainService.getDomain(domainCode);
        if (domain == null) {
            throw new DomainException("Could get current domain: domain with code [{}] is not configured");
        }
        return domain;
    }

    @Override
    public Domain getCurrentDomainSafely() {
        Domain result = null;
        try {
            result = getCurrentDomain();
        } catch (DomainException e) {
            LOG.trace("Could not get current domain", e);
        }
        return result;

    }

    @Override
    public void setCurrentDomain(String domainCode) {
        if (StringUtils.isEmpty(domainCode)) {
            throw new DomainException("Could not set current domain: domain is empty");
        }

        LOG.putMDC(DomibusLogger.MDC_DOMAIN, domainCode);
        LOG.trace("Set domain to [{}]", domainCode);
    }

    @Override
    public void setCurrentDomain(Domain domain) {
        setCurrentDomain(domain.getCode());
    }

    @Override
    public void clearCurrentDomain() {
        LOG.removeMDC(DomibusLogger.MDC_DOMAIN);
    }
}

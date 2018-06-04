package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * @author Cosmin Baciu
 * @since 1.2
 */
public interface DomainContextExtService {

    DomainDTO getCurrentDomain();

    /**
     * Get the current domain. Does not throw exceptions in the attempt to get the domain.
     *
     * @return
     */
    DomainDTO getCurrentDomainSafely();

    void setCurrentDomain(DomainDTO domain);

    void clearCurrentDomain();
}

package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainContextExtService {

    DomainDTO getCurrentDomain();

    /**
     * Get the current domain. Does not throw exceptions in the attempt to get the domain.
     *
     * @return the current domain or null if there is no current domain set
     */
    DomainDTO getCurrentDomainSafely();

    void setCurrentDomain(DomainDTO domain);

    void clearCurrentDomain();
}

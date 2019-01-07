package eu.domibus.api.multitenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the Runnable class to be executed. Sets first the domain on the thread before execution.
 *
 * @author Thomas Dussart
 * @since 4.0.1
 */
public class DomainRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DomainRunnable.class);

    protected DomainContextProvider domainContextProvider;
    protected Runnable runnable;
    protected Domain domain;

    public DomainRunnable(final DomainContextProvider domainContextProvider, final Domain domain, final Runnable runnable) {
        this.domainContextProvider = domainContextProvider;
        this.runnable = runnable;
        this.domain = domain;
    }

    @Override
    public void run() {
        domainContextProvider.setCurrentDomain(domain);
        runnable.run();
    }
}

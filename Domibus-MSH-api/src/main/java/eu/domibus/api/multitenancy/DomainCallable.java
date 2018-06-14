package eu.domibus.api.multitenancy;

import java.util.concurrent.Callable;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class DomainCallable<T> implements Callable<T> {

    protected DomainContextProvider domainContextProvider;
    protected Callable<T> callable;

    public DomainCallable(DomainContextProvider domainContextProvider, Callable<T> callable) {
        this.domainContextProvider = domainContextProvider;
        this.callable = callable;
    }

    @Override
    public T call() throws Exception {
        domainContextProvider.clearCurrentDomain();
        return callable.call();
    }
}

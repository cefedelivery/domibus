package eu.domibus.api.multitenancy;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainContextProvider {

    Domain getCurrentDomain();
}

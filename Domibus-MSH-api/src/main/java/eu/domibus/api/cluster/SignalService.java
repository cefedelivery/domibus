package eu.domibus.api.cluster;

import eu.domibus.api.multitenancy.Domain;

/**
 * Interface for signal commands into a cluster configuration
 * We are using a {@JMS topic} implementation
 *
 * @author Catalin Enache
 * @since 4.1
 */
public interface SignalService {

    /**
     * signals trust store to be update on
     * @param domain
     */
    void signalTrustStoreUpdate(Domain domain);

    /**
     * signals PMode update to other servers in the cluster
     */
    void signalPModeUpdate();

    /**
     * signals Logging set level to other servers in the cluster
     *
     * @param name
     * @param level
     */
    void  signalLoggingSetLevel(final String name, final String level);

    /**
     * signals Logging reset to other servers in the cluster
     */
    void signalLoggingReset();

}

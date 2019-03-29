package eu.domibus.core.mpc;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author idragusa
 * @since 4.1
 */
@Service
public class MpcServiceImpl implements MpcService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MpcServiceImpl.class);

    protected static final String DOMIBUS_PULL_FORCE_BY_MPC = "domibus.pull.force_by_mpc";

    protected static final String DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR = "domibus.pull.mpc_initiator_separator";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public boolean forcePullOnMpc(String mpc) {
        if (mpc == null) {
            return false;
        }
        if (!domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_PULL_FORCE_BY_MPC)) {
            return false;
        }
        // Result is true when DOMIBUS_PULL_FORCE_BY_MPC is true AND the mpc contains the separator
        String separator = domibusPropertyProvider.getDomainProperty(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR);
        if (separator != null && mpc.contains(separator)) {
            return true;
        }
        return false;
    }

    @Override
    public String extractInitiator(String mpc) {
        if (mpc == null) {
            return null;
        }
        String separator = domibusPropertyProvider.getDomainProperty(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR);
        try {
            return mpc.substring(mpc.indexOf(separator) + separator.length() + 1); // +1 for the final '/'
        }catch (StringIndexOutOfBoundsException exc) {
            LOG.error("Invalid mpc value [{}]", mpc);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_007, "Invalid mpc value " + mpc);
        }
    }

    @Override
    public String extractBaseMpc(String mpc) {
        if (mpc == null) {
            return null;
        }
        String separator = domibusPropertyProvider.getDomainProperty(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR);
        try {
            return mpc.substring(0, mpc.indexOf(separator) - 1); // -1 for the '/'
        }catch (StringIndexOutOfBoundsException exc) {
            LOG.error("Invalid mpc value [{}]", mpc);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_007, "Invalid mpc value " + mpc);
        }
    }
}

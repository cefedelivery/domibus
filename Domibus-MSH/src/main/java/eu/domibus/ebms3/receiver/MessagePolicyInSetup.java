package eu.domibus.ebms3.receiver;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 */
//@thom test this hierarchy of class
public interface MessagePolicyInSetup {
    LegConfiguration extractMessageConfiguration() throws EbMS3Exception;
    void accept(PolicyInSetupVisitor visitor);
}

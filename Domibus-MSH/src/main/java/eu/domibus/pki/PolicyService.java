
package eu.domibus.pki;

import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.model.configuration.LegConfiguration;
import org.apache.neethi.Policy;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author Arun Raj
 * @since 3.3
 */
public interface PolicyService {

    boolean isNoSecurityPolicy(Policy policy);

    @Cacheable("policyCache")
    Policy parsePolicy(final String location) throws ConfigurationException;

    /*
    * Returns the security policy based on the leg configured in the pMode
     */
    Policy getPolicy(final LegConfiguration legConfiguration) throws ConfigurationException;

}

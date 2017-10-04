
package eu.domibus.pki;

import eu.domibus.common.exception.ConfigurationException;
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


}

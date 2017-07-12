package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.Configuration;

import java.util.List;

/**
 * @author musatmi
 * @since 3.3
 */
public interface ConfigurationValidator {
    List<String> validate(Configuration configuration);
}

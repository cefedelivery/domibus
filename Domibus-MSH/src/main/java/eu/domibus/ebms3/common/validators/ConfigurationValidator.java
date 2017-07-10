package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.Configuration;

import java.util.List;

/**
 * Created by musatmi on 10/07/2017.
 */
public interface ConfigurationValidator {
    List<String> validate(Configuration configuration);
}

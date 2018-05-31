package eu.domibus.common.converters;

import eu.domibus.api.user.User;

import java.util.List;

/**
 * Class responsible of conversion from the user entity to the corresponding api user object
 *
 * @author Ion Perpegel
 * @since 4.0
 */
public interface UserConverter {

    User convert(eu.domibus.common.model.security.User source);

    List<User> convert(List<eu.domibus.common.model.security.User> sourceList);
}


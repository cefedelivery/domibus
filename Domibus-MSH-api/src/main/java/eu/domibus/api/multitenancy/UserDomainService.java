package eu.domibus.api.multitenancy;

import eu.domibus.api.user.User;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface UserDomainService {

    String getDomainForUser(String user);

    String getPreferredDomainForUser(String user);

    List<User> getSuperUsers();
}

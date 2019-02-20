package eu.domibus.security;

import eu.domibus.common.model.security.UserDetail;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public interface AuthenticationService {

    UserDetail authenticate(String username, String password, String domain);

    void changeDomain(String domainCode);
}

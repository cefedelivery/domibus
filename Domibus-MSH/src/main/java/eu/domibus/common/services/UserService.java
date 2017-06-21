package eu.domibus.common.services;

import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserService extends UserDetailsService {

    List<User> findUsers();

    void saveUsers(List<eu.domibus.api.user.User> users);
}

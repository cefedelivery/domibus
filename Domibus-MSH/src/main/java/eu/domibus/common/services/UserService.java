package eu.domibus.common.services;

import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.web.rest.ro.UserResponseRO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserService {

    List<User> findUsers();

    List<UserRole> findRoles();

    @Transactional
    void createUser(User user);

    @Transactional
    void updateUser(User user);
}

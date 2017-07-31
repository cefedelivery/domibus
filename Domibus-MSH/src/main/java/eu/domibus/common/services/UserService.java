package eu.domibus.common.services;

import eu.domibus.api.user.User;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserService{

    /**
     * @return the list of system users.
     */
    List<eu.domibus.api.user.User> findUsers();

    /**
     * create or update users of the system (edited in the user management gui console).
     * @param users to create of update.
     */
    void saveUsers(List<eu.domibus.api.user.User> users);

    /**
     * get all user roles
     */
    List<eu.domibus.api.user.UserRole> findUserRoles();

    /**
     * update users
     */
    void updateUsers(List<User> users);
}

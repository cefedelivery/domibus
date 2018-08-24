package eu.domibus.common.services;

import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthType;
import eu.domibus.core.security.AuthenticationEntity;

import java.util.List;

/**
 * @author Pion
 * @since 4.0
 */
public interface PluginUserService {

    /**
     * Search the plugin users. The search is made based on the following
     * criteria.
     *
     * @param authType criteria to search on the authentication type of the user (BASIC or CERTIFICATE)
     * @param authRole criteria to search on the authentication role of the user (ROLE_ADMIN or ROLE_USER)
     * @param originalUser criteria to search by originalUser
     * @param userName criteria to search by userName
     * @param pageStart pagination start
     * @param pageSize page size.
     * @return a list of plugin users.
     */
    List<AuthenticationEntity> findUsers(AuthType authType,
            AuthRole authRole,
            String originalUser,
            String userName,
            int pageStart,
            int pageSize);

    /**
     * Counts the plugin users matching the search criteria.
     *
     * @param authType criteria to search on the authentication type of the user (BASIC or CERTIFICATE)
     * @param authRole criteria to search on the authentication role of the user (ROLE_ADMIN or ROLE_USER)
     * @param originalUser criteria to search by originalUser
     * @param userName criteria to search by userName
     * @return the count of matching plugin users.
     */ 
    long countUsers(AuthType authType, AuthRole authRole, String originalUser, String userName);

    /**
     * Update the plugin users. The lists of users to create, update, and delete
     * are provided.
     *
     * @param addedUsers New users to create
     * @param updatedUsers Existing users to update
     * @param removedUsers Existing users to delete
     */
    void updateUsers(List<AuthenticationEntity> addedUsers, List<AuthenticationEntity> updatedUsers, List<AuthenticationEntity> removedUsers);
}

package eu.domibus.common.services;

import java.util.List;

/**
 *
 * @author Ion Perpegel
 * @since 4.0
 *
 */
public interface UserPersistenceService {
    void updateUsers(List<eu.domibus.api.user.User> users);
    void changePassword(String userName, String currentPassword, String newPassword);
}

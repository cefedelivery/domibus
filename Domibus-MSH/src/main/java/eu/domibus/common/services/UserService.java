package eu.domibus.common.services;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.user.User;
import eu.domibus.common.model.security.UserLoginErrorReason;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserService {

    /**
     * @return the list of system users.
     */
    List<eu.domibus.api.user.User> findUsers();

    /**
     * Get all user roles
     * @return all user roles
     */
    List<eu.domibus.api.user.UserRole> findUserRoles();

    /**
     * Create or update users of the system (edited in the user management gui console).
     * @param users to create of update.
     */
    void updateUsers(List<User> users);

    /**
     * Handle the account lockout policy.
     * Will log login attempt to the security log and inactivate user after certain amount of login attempt.
     *
     * @param userName the user loggin string
     * @return the reason of the login error.
     */
    UserLoginErrorReason handleWrongAuthentication(final String userName);

    /**
     * Search for all users that have been suspended (due to multiple unsuccessful login attempts)
     * and verify if the suspension date is smaller then current time - interval period defined in property file.
     * If some user are found they will be reactivated.
     */
    void reactivateSuspendedUsers();

    /**
     * Verify if user add some incorrect login attempt and reset the attempt counter.
     * @param username the userName
     */
    void handleCorrectAuthentication(String username);

    /**
     * Verify if the user's password is expired.
     * @param username the userName
     */
    void validateExpiredPassword(String username);

    /**
     * Verify if the user's password is "almost" expired
     * @param username the userName
     * @return the days till expiration; null if it is not to raise a warning
     */
    Integer validateDaysTillExpiration(String username);


    public void sendAlerts();

}

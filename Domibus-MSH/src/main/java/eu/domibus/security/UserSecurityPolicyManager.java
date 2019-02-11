package eu.domibus.security;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.UserBase;
import eu.domibus.common.dao.security.UserDaoBase;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.common.model.security.UserPasswordHistory;
import eu.domibus.core.alerts.service.UserAlertsService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Service
public abstract class UserSecurityPolicyManager<U extends UserEntityBase> {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserSecurityPolicyManager.class);

    private static final String CREDENTIALS_EXPIRED = "Expired";

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private BCryptPasswordEncoder bCryptEncoder;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainService domainService;


    // abstract methods
    protected abstract String getPasswordComplexityPatternProperty();

    public abstract String getPasswordHistoryPolicyProperty();

    protected abstract String getMaximumDefaultPasswordAgeProperty();

    protected abstract String getMaximumPasswordAgeProperty();

    protected abstract String getWarningDaysBeforeExpiration();

    protected abstract UserPasswordHistoryDao getUserHistoryDao();

    protected abstract UserDaoBase getUserDao();

    protected abstract int getMaxAttemptAmount(UserEntityBase user);

    protected abstract UserAlertsService getUserAlertsService();

    protected abstract int getSuspensionInterval();

    // public interface
    public void validateComplexity(final String userName, final String password) throws DomibusCoreException {

        String errorMessage = "The password of " + userName + " user does not meet the minimum complexity requirements";
        if (StringUtils.isBlank(password)) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
        }

        String passwordPattern = domibusPropertyProvider.getOptionalDomainProperty(getPasswordComplexityPatternProperty());
        if (StringUtils.isBlank(passwordPattern)) {
            return;
        }

        Pattern patternNoControlChar = Pattern.compile(passwordPattern);
        Matcher m = patternNoControlChar.matcher(password);
        if (!m.matches()) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
        }
    }

    public void validateHistory(final String userName, final String password) throws DomibusCoreException {

        int oldPasswordsToCheck = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(getPasswordHistoryPolicyProperty()));
        if (oldPasswordsToCheck == 0) {
            return;
        }

        UserEntityBase user = getUserDao().findByUserName(userName);
        List<UserPasswordHistory> oldPasswords = getUserHistoryDao().getPasswordHistory(user, oldPasswordsToCheck);
        if (oldPasswords.stream().anyMatch(userHistoryEntry -> bCryptEncoder.matches(password, userHistoryEntry.getPasswordHash()))) {
            String errorMessage = "The password of " + userName + " user cannot be the same as the last " + oldPasswordsToCheck;
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
        }
    }

    public void validatePasswordExpired(String userName, boolean isDefaultPassword, LocalDateTime passwordChangeDate) {

        String expirationProperty = isDefaultPassword ? getMaximumDefaultPasswordAgeProperty() : getMaximumPasswordAgeProperty();
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));
        LOG.debug("Password expiration policy for user [{}] : {} days", userName, maxPasswordAgeInDays);

        if (maxPasswordAgeInDays <= 0) {
            return;
        }

        LocalDate expirationDate = passwordChangeDate == null ? LocalDate.now() : passwordChangeDate.plusDays(maxPasswordAgeInDays).toLocalDate();

        if (expirationDate.isBefore(LocalDate.now())) {
            LOG.debug("Password expired for user [{}]", userName);
            throw new CredentialsExpiredException(CREDENTIALS_EXPIRED);
        }
    }

    public Integer getDaysTillExpiration(String userName, boolean isDefaultPassword, LocalDateTime passwordChangeDate) {
        int warningDaysBeforeExpiration = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(getWarningDaysBeforeExpiration()));
        if (warningDaysBeforeExpiration <= 0) {
            return null;
        }

        String expirationProperty = isDefaultPassword ? getMaximumDefaultPasswordAgeProperty() : getMaximumPasswordAgeProperty();
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));

        if (maxPasswordAgeInDays <= 0) {
            return null;
        }

        if (warningDaysBeforeExpiration >= maxPasswordAgeInDays) {
            LOG.warn("Password policy: days until expiration for user [{}] is greater that max age.", userName);
            return null;
        }

        LocalDate passwordDate = passwordChangeDate.toLocalDate();
        if (passwordDate == null) {
            LOG.debug("Password policy: expiration date for user [{}] is not set", userName);
            return null;
        }

        LocalDate expirationDate = passwordDate.plusDays(maxPasswordAgeInDays);
        LocalDate today = LocalDate.now();
        int daysUntilExpiration = (int) ChronoUnit.DAYS.between(today, expirationDate);
        
        LOG.debug("Password policy: days until expiration for user [{}] : {} days", userName, daysUntilExpiration);

        if (0 <= daysUntilExpiration && daysUntilExpiration <= warningDaysBeforeExpiration) {
            return daysUntilExpiration;
        } else {
            return null;
        }
    }

    public void changePassword(U user, String newPassword) {
        // save old password in history
        savePasswordHistory(user);

        String userName = user.getUserName();
        validateComplexity(userName, newPassword);
        validateHistory(userName, newPassword);

        user.setPassword(bCryptEncoder.encode(newPassword));
        user.setDefaultPassword(false);
    }

    private void savePasswordHistory(U user) {
        int passwordsToKeep = domibusPropertyProvider.getIntegerOptionalDomainProperty(getPasswordHistoryPolicyProperty());
        if (passwordsToKeep <= 0) {
            return;
        }

        UserPasswordHistoryDao dao = getUserHistoryDao();
        dao.savePassword(user, user.getPassword(), user.getPasswordChangeDate());
        dao.removePasswords(user, passwordsToKeep - 1);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCorrectAuthentication(final String userName) {
        UserEntityBase user = getUserDao().findByUserName(userName);
        LOG.debug("handleCorrectAuthentication for user [{}]", userName);
        if (user.getAttemptCount() > 0) {
            LOG.debug("user [{}] has [{}] attempts. Resetting to 0. ", userName, user.getAttemptCount());
            user.setAttemptCount(0);
            getUserDao().update(user, true);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserLoginErrorReason handleWrongAuthentication(final String userName) {
        UserEntityBase user = getUserDao().findByUserName(userName);

        UserLoginErrorReason userLoginErrorReason = getLoginFailureReason(userName, user);

        if (UserLoginErrorReason.BAD_CREDENTIALS == userLoginErrorReason) {
            applyLockingPolicyOnLogin(user);
        }

        getUserAlertsService().triggerLoginEvents(userName, userLoginErrorReason);
        return userLoginErrorReason;
    }

    protected UserLoginErrorReason getLoginFailureReason(String userName, UserEntityBase user) {
        if (user == null) {
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_UNKNOWN_USER, userName);
            return UserLoginErrorReason.UNKNOWN;
        }
        if (!user.isActive()) {
            if (user.getSuspensionDate() == null) {
                LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_INACTIVE_USER, userName);
                return UserLoginErrorReason.INACTIVE;
            } else {
                LOG.securityWarn(DomibusMessageCode.SEC_CONSOLE_LOGIN_SUSPENDED_USER, userName);
                return UserLoginErrorReason.SUSPENDED;
            }
        }

        LOG.securityWarn(DomibusMessageCode.SEC_CONSOLE_LOGIN_BAD_CREDENTIALS, userName);
        return UserLoginErrorReason.BAD_CREDENTIALS;
    }

    protected void applyLockingPolicyOnLogin(UserEntityBase user) {
        int maxAttemptAmount = getMaxAttemptAmount(user);

        user.setAttemptCount(user.getAttemptCount() + 1);

        if (user.getAttemptCount() >= maxAttemptAmount) {
            LOG.debug("Applying account locking policy, max number of attempt ([{}]) reached for user [{}]", maxAttemptAmount, user.getUserName());
            user.setActive(false);
            user.setSuspensionDate(new Date(System.currentTimeMillis()));
            LOG.securityWarn(DomibusMessageCode.SEC_CONSOLE_LOGIN_LOCKED_USER, user.getUserName(), maxAttemptAmount);

            getUserAlertsService().triggerDisabledEvent(user);
        }

        getUserDao().update(user, true);
    }

    public UserEntityBase applyLockingPolicyOnUpdate(UserBase user) {
        UserEntityBase userEntity = getUserDao().findByUserName(user.getUserName());
        if (!userEntity.isActive() && user.isActive()) {
            userEntity.setSuspensionDate(null);
            userEntity.setAttemptCount(0);
        } else if (!user.isActive() && userEntity.isActive()) {
            LOG.debug("User:[{}] has been disabled by administrator", user.getUserName());
            getUserAlertsService().triggerDisabledEvent(user);
        }
        userEntity.setActive(user.isActive());
        return userEntity;
    }

    @Transactional
    public void reactivateSuspendedUsers() {
        int suspensionInterval = getSuspensionInterval();

        //user will not be reactivated.
        if (suspensionInterval <= 0) {
            return;
        }

        Date currentTimeMinusSuspensionInterval = new Date(System.currentTimeMillis() - (suspensionInterval * 1000));

        List<UserEntityBase> users = getUserDao().getSuspendedUsers(currentTimeMinusSuspensionInterval);
        for (UserEntityBase user : users) {
            LOG.debug("Suspended user [{}] of type [{}] is going to be reactivated.", user.getUserName(), user.getType().getName());

            user.setSuspensionDate(null);
            user.setAttemptCount(0);
            user.setActive(true);
        }

        getUserDao().update(users);
    }

}

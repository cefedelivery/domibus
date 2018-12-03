package eu.domibus.common.validators;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.model.security.IUser;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Service
public abstract class UserPasswordManager<T extends IUser> {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserPasswordManager.class);

    private static final String CREDENTIALS_EXPIRED = "Expired";

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    protected abstract String getPasswordComplexityPatternProperty();

    public abstract String getPasswordHistoryPolicyProperty();

    protected abstract List<String> getPasswordHistory(String userName, int oldPasswordsToCheck);

    protected abstract String getMaximumDefaultPasswordAgeProperty();

    protected abstract String getMaximumPasswordAgeProperty();

    protected abstract String getWarningDaysBeforeExpiration();

    protected abstract UserPasswordHistoryDao getUserHistoryDao();


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

        List<String> oldPasswords = getPasswordHistory(userName, oldPasswordsToCheck);
        if (oldPasswords.stream().anyMatch(pass -> bcryptEncoder.matches(password, pass))) {
            String errorMessage = "The password of " + userName + " user cannot be the same as the last " + oldPasswordsToCheck;
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
        }
    }

    public void validatePasswordExpired(String userName, boolean isDefaultPassword, LocalDateTime passwordChangeDate) {

        String expirationProperty = isDefaultPassword ? getMaximumDefaultPasswordAgeProperty() : getMaximumPasswordAgeProperty();
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));
        LOG.debug("Password expiration policy for user [{}] : {} days", userName, maxPasswordAgeInDays);

        if (maxPasswordAgeInDays == 0) {
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
        if (warningDaysBeforeExpiration == 0) {
            return null;
        }

        String expirationProperty = isDefaultPassword ? getMaximumDefaultPasswordAgeProperty() : getMaximumPasswordAgeProperty();
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));

        if (maxPasswordAgeInDays == 0) {
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
        int daysUntilExpiration = Period.between(today, expirationDate).getDays();

        LOG.debug("Password policy: days until expiration for user [{}] : {} days", userName, daysUntilExpiration);

        if (0 <= daysUntilExpiration && daysUntilExpiration <= warningDaysBeforeExpiration) {
            return daysUntilExpiration;
        } else {
            return null;
        }
    }

    public void changePassword(T user, String newPassword) {

        savePasswordHistory(user); // save old password in history

        String userName = user.getUserName();
        validateComplexity(userName, newPassword);
        validateHistory(userName, newPassword);

        user.setPassword(bcryptEncoder.encode(newPassword));
        user.setDefaultPassword(false);
    }

    private void savePasswordHistory(T user) {
        int passwordsToKeep = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(getPasswordHistoryPolicyProperty(), "0"));
        if (passwordsToKeep == 0) {
            return;
        }

        UserPasswordHistoryDao dao = getUserHistoryDao();
        dao.savePassword(user, user.getPassword(), user.getPasswordChangeDate());
        dao.removePasswords(user, passwordsToKeep - 1);

        //savePasswordHistory(user, passwordsToKeep);
    }

    protected void savePasswordHistory(T user, int passwordsToKeep) {
        UserPasswordHistoryDao dao = getUserHistoryDao();
        dao.savePassword(user, user.getPassword(), user.getPasswordChangeDate());
        dao.removePasswords(user, passwordsToKeep - 1);
    }

}

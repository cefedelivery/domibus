package eu.domibus.common.validators;

import eu.domibus.common.dao.security.ConsoleUserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserDaoBase;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.model.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Service
public class ConsoleUserPasswordManager extends UserPasswordManager<User> {
    final static String WARNING_DAYS_BEFORE_EXPIRATION = "domibus.passwordPolicy.warning.beforeExpiration";

    static final String PASSWORD_COMPLEXITY_PATTERN = "domibus.passwordPolicy.pattern";
    static final String PASSWORD_HISTORY_POLICY = "domibus.passwordPolicy.dontReuseLast";

    final static String MAXIMUM_PASSWORD_AGE = "domibus.passwordPolicy.expiration";
    final static String MAXIMUM_DEFAULT_PASSWORD_AGE = "domibus.passwordPolicy.defaultPasswordExpiration";

    @Autowired
    protected UserDao userDao;

    @Autowired
    private ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Override
    protected String getPasswordComplexityPatternProperty() {
        return PASSWORD_COMPLEXITY_PATTERN;
    }

    @Override
    public String getPasswordHistoryPolicyProperty() {
        return PASSWORD_HISTORY_POLICY;
    }

    @Override
    public String getMaximumDefaultPasswordAgeProperty() { return MAXIMUM_DEFAULT_PASSWORD_AGE; }

    @Override
    protected String getMaximumPasswordAgeProperty() { return MAXIMUM_PASSWORD_AGE; }

    @Override
    public String getWarningDaysBeforeExpiration() {
        return WARNING_DAYS_BEFORE_EXPIRATION;
    }

    @Override
    protected UserPasswordHistoryDao getUserHistoryDao() {
        return userPasswordHistoryDao;
    }

    @Override
    protected UserDaoBase getUserDao() {
        return userDao;
    }

}

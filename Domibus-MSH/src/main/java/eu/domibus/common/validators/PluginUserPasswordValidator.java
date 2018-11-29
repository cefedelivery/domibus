package eu.domibus.common.validators;

import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.core.security.AuthenticationEntity;
import eu.domibus.core.security.PluginUserPasswordHistory;
import eu.domibus.core.security.PluginUserPasswordHistoryDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Service
public class PluginUserPasswordValidator extends PasswordValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserPasswordValidator.class);

    final static String WARNING_DAYS_BEFORE_EXPIRATION = "domibus.plugin_passwordPolicy.warning.beforeExpiration";

    static final String PASSWORD_COMPLEXITY_PATTERN = "domibus.plugin_passwordPolicy.pattern";
    static final String PASSWORD_HISTORY_POLICY = "domibus.plugin_passwordPolicy.dontReuseLast";

    final static String MAXIMUM_PASSWORD_AGE = "domibus.plugin_passwordPolicy.expiration";
    final static String MAXIMUM_DEFAULT_PASSWORD_AGE = "domibus.plugin_passwordPolicy.defaultPasswordExpiration";

    @Autowired
    protected AuthenticationDAO userDao;

    @Autowired
    private PluginUserPasswordHistoryDao userPasswordHistoryDao;

    @Override
    protected String getPasswordComplexityPatternProperty() {
        return PASSWORD_COMPLEXITY_PATTERN;
    }

    @Override
    public String getPasswordHistoryPolicyProperty() {
        return PASSWORD_HISTORY_POLICY;
    }

    @Override
    protected String getMaximumDefaultPasswordAgeProperty() { return MAXIMUM_DEFAULT_PASSWORD_AGE; }

    @Override
    protected String getMaximumPasswordAgeProperty() { return MAXIMUM_PASSWORD_AGE; }

    protected List<String> getPasswordHistory(String userName, int oldPasswordsToCheck) {
        AuthenticationEntity user = userDao.findByUser(userName);
        List<PluginUserPasswordHistory> oldPasswords = userPasswordHistoryDao.getPasswordHistory(user, oldPasswordsToCheck);
        return oldPasswords.stream().map(el -> el.getPasswordHash()).collect(Collectors.toList());
    }

    @Override
    protected String getWarningDaysBeforeExpiration() {
        return WARNING_DAYS_BEFORE_EXPIRATION;
    }

}

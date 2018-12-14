package eu.domibus.security;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.UserDaoBase;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.alerts.service.UserAlertsService;
import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.core.security.AuthenticationEntity;
import eu.domibus.core.security.PluginUserPasswordHistoryDao;
import eu.domibus.security.UserSecurityPolicyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Service
public class PluginUserSecurityPolicyManager extends UserSecurityPolicyManager<AuthenticationEntity> {

    final static String WARNING_DAYS_BEFORE_EXPIRATION = "domibus.plugin.passwordPolicy.warning.beforeExpiration";

    static final String PASSWORD_COMPLEXITY_PATTERN = "domibus.plugin.passwordPolicy.pattern";
    static final String PASSWORD_HISTORY_POLICY = "domibus.plugin.passwordPolicy.dontReuseLast";

    final static String MAXIMUM_PASSWORD_AGE = "domibus.plugin.passwordPolicy.expiration";
    final static String MAXIMUM_DEFAULT_PASSWORD_AGE = "domibus.plugin.passwordPolicy.defaultPasswordExpiration";

    protected static final String MAXIMUM_LOGIN_ATTEMPT = "domibus.plugin.login.maximum.attempt";

    protected static final String LOGIN_SUSPENSION_TIME = "domibus.plugin.login.suspension.time";

    @Autowired
    protected AuthenticationDAO userDao;

    @Autowired
    private PluginUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private PluginUserAlertsServiceImpl userAlertsService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;


    @Override
    protected String getPasswordComplexityPatternProperty() {
        return PASSWORD_COMPLEXITY_PATTERN;
    }

    @Override
    public String getPasswordHistoryPolicyProperty() {
        return PASSWORD_HISTORY_POLICY;
    }

    @Override
    protected String getMaximumDefaultPasswordAgeProperty() {
        return MAXIMUM_DEFAULT_PASSWORD_AGE;
    }

    @Override
    protected String getMaximumPasswordAgeProperty() {
        return MAXIMUM_PASSWORD_AGE;
    }

    @Override
    protected String getWarningDaysBeforeExpiration() {
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

    @Override
    protected int getMaxAttemptAmount(UserEntityBase user) {
        return domibusPropertyProvider.getIntegerProperty(MAXIMUM_LOGIN_ATTEMPT);
    }

    @Override
    protected UserAlertsService getUserAlertsService() {
        return userAlertsService;
    }

    @Override
    protected int getSuspensionInterval() {
        return domibusPropertyProvider.getIntegerDomainProperty(LOGIN_SUSPENSION_TIME);
    }

}

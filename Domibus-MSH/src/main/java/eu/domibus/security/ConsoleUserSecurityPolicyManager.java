package eu.domibus.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.ConsoleUserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserDaoBase;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.alerts.service.UserAlertsService;
import eu.domibus.security.UserSecurityPolicyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Service
public class ConsoleUserSecurityPolicyManager extends UserSecurityPolicyManager<User> {
    final static String WARNING_DAYS_BEFORE_EXPIRATION = "domibus.passwordPolicy.warning.beforeExpiration";

    static final String PASSWORD_COMPLEXITY_PATTERN = "domibus.passwordPolicy.pattern";
    static final String PASSWORD_HISTORY_POLICY = "domibus.passwordPolicy.dontReuseLast";

    final static String MAXIMUM_PASSWORD_AGE = "domibus.passwordPolicy.expiration";
    final static String MAXIMUM_DEFAULT_PASSWORD_AGE = "domibus.passwordPolicy.defaultPasswordExpiration";

    protected static final String MAXIMUM_LOGIN_ATTEMPT = "domibus.console.login.maximum.attempt";

    protected static final String LOGIN_SUSPENSION_TIME = "domibus.console.login.suspension.time";


    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected UserDao userDao;

    @Autowired
    private ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private ConsoleUserAlertsServiceImpl userAlertsService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    protected String getPasswordComplexityPatternProperty() {
        return PASSWORD_COMPLEXITY_PATTERN;
    }

    @Override
    public String getPasswordHistoryPolicyProperty() {
        return PASSWORD_HISTORY_POLICY;
    }

    @Override
    public String getMaximumDefaultPasswordAgeProperty() {
        return MAXIMUM_DEFAULT_PASSWORD_AGE;
    }

    @Override
    protected String getMaximumPasswordAgeProperty() {
        return MAXIMUM_PASSWORD_AGE;
    }

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

    @Override
    protected int getMaxAttemptAmount(UserEntityBase user) {
        final Domain domain = getCurrentOrDefaultDomainForUser((User) user);
        return domibusPropertyProvider.getIntegerDomainProperty(domain, MAXIMUM_LOGIN_ATTEMPT);
    }

    @Override
    protected UserAlertsService getUserAlertsService() {
        return userAlertsService;
    }

    @Override
    protected int getSuspensionInterval() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();

        int suspensionInterval;
        if (domain == null) { //it is called for super-users so we read from default domain
            suspensionInterval = domibusPropertyProvider.getIntegerProperty(LOGIN_SUSPENSION_TIME);
        } else { //for normal users the domain is set as current Domain
            suspensionInterval = domibusPropertyProvider.getIntegerDomainProperty(LOGIN_SUSPENSION_TIME);
        }
        return suspensionInterval;
    }

    private Domain getCurrentOrDefaultDomainForUser(User user) {
        String domainCode;
        boolean isSuperAdmin = user.isSuperAdmin();
        if (isSuperAdmin) {
            domainCode = DomainService.DEFAULT_DOMAIN.getCode();
        } else {
            domainCode = userDomainService.getDomainForUser(user.getUserName());
        }
        return domainService.getDomain(domainCode);
    }

}

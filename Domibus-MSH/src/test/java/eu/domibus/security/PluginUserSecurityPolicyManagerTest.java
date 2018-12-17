package eu.domibus.security;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.core.security.PluginUserPasswordHistoryDao;
import eu.domibus.security.PluginUserSecurityPolicyManager;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class PluginUserSecurityPolicyManagerTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PluginUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    AuthenticationDAO userDao;

    @Injectable
    BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    PluginUserAlertsServiceImpl userAlertsService;

    @Injectable
    UserDomainService userDomainService;

    @Injectable
    DomainService domainService;

    @Tested
    PluginUserSecurityPolicyManager passwordManager;

    @Test
    public void testGetPasswordComplexityPatternProperty() {
        String result = passwordManager.getPasswordComplexityPatternProperty();
        Assert.assertEquals(PluginUserSecurityPolicyManager.PASSWORD_COMPLEXITY_PATTERN, result);
    }

    @Test
    public void testGetPasswordHistoryPolicyProperty() {
        String result = passwordManager.getPasswordHistoryPolicyProperty();
        Assert.assertEquals(PluginUserSecurityPolicyManager.PASSWORD_HISTORY_POLICY, result);
    }

    @Test
    public void testGetMaximumDefaultPasswordAgeProperty() {
        String result = passwordManager.getMaximumDefaultPasswordAgeProperty();
        Assert.assertEquals(PluginUserSecurityPolicyManager.MAXIMUM_DEFAULT_PASSWORD_AGE, result);
    }
    @Test
    public void testGetMaximumPasswordAgeProperty() {
        String result = passwordManager.getMaximumPasswordAgeProperty();
        Assert.assertEquals(PluginUserSecurityPolicyManager.MAXIMUM_PASSWORD_AGE, result);
    }
    @Test
    public void testGetWarningDaysBeforeExpiration() {
        String result = passwordManager.getWarningDaysBeforeExpiration();
        Assert.assertEquals(PluginUserSecurityPolicyManager.WARNING_DAYS_BEFORE_EXPIRATION, result);
    }
}

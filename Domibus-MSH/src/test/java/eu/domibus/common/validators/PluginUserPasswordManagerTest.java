package eu.domibus.common.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.core.security.AuthenticationEntity;
import eu.domibus.core.security.PluginUserPasswordHistory;
import eu.domibus.core.security.PluginUserPasswordHistoryDao;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class PluginUserPasswordManagerTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PluginUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    AuthenticationDAO userDao;

    @Injectable
    BCryptPasswordEncoder bcryptEncoder;

    @Tested
    PluginUserPasswordManager passwordManager;

    @Test
    public void testGetPasswordComplexityPatternProperty() {
        String result = passwordManager.getPasswordComplexityPatternProperty();
        Assert.assertEquals(PluginUserPasswordManager.PASSWORD_COMPLEXITY_PATTERN, result);
    }

    @Test
    public void testGetPasswordHistoryPolicyProperty() {
        String result = passwordManager.getPasswordHistoryPolicyProperty();
        Assert.assertEquals(PluginUserPasswordManager.PASSWORD_HISTORY_POLICY, result);
    }

    @Test
    public void testGetMaximumDefaultPasswordAgeProperty() {
        String result = passwordManager.getMaximumDefaultPasswordAgeProperty();
        Assert.assertEquals(PluginUserPasswordManager.MAXIMUM_DEFAULT_PASSWORD_AGE, result);
    }
    @Test
    public void testGetMaximumPasswordAgeProperty() {
        String result = passwordManager.getMaximumPasswordAgeProperty();
        Assert.assertEquals(PluginUserPasswordManager.MAXIMUM_PASSWORD_AGE, result);
    }
    @Test
    public void testGetWarningDaysBeforeExpiration() {
        String result = passwordManager.getWarningDaysBeforeExpiration();
        Assert.assertEquals(PluginUserPasswordManager.WARNING_DAYS_BEFORE_EXPIRATION, result);
    }
}

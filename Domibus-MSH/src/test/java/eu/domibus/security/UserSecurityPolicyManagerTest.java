package eu.domibus.security;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.ConsoleUserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import mockit.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class UserSecurityPolicyManagerTest {

    private static final String PASSWORD_COMPLEXITY_PATTERN = "^.*(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&+=\\-_<>.,?:;*/()|\\[\\]{}'\"\\\\]).{8,32}$";

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    ConsoleUserAlertsServiceImpl userAlertsService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    UserDao userDao;

    @Injectable
    BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    UserDomainService userDomainService;

    @Injectable
    DomainService domainService;

    @Injectable
    private MultiDomainAlertConfigurationService alertConfigurationService;
    
    @Tested
    UserSecurityPolicyManager securityPolicyManager;

    @Test
    public void checkPasswordComplexity() throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
//            domibusPropertyProvider.getOptionalDomainProperty("domibus.passwordPolicy.warning.beforeExpiration");
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

//        Password should follow all of these rules:
//        - Minimum length: 8 characters
//        - Maximum length: 32 characters
//        - At least one letter in lowercase
//        - At least one letter in uppercase
//        - At least one digit
//        - At least one special character



        // Happy Flow: No error should occur
        try {
            String testPassword1 = "Lalala-5";
            securityPolicyManager.validateComplexity(userName, testPassword1);

            String testPassword2 = "UPPER lower 12345 /`~!@#$%^&*()-"; // 32 characters
            securityPolicyManager.validateComplexity(userName, testPassword2);

            String testPassword3 = "Aa`()-_=+\\|,<.>/?;:'\"|\\[{]}.0";
            securityPolicyManager.validateComplexity(userName, testPassword3);

            char[] specialCharacters = new char[]{'~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '+', '=', '-', '_',
                    '|', '(', ')', '[', ']', '{', '}', '?', '/', ';', ':', ',', '.', '<', '>', '\'', '\"', '\\'};
            for (char c : specialCharacters) {
                String testPassword4 = "AlphaNum3ric " + c;
                securityPolicyManager.validateComplexity(userName, testPassword4);
            }

        } catch (DomibusCoreException e1) {
            Assert.fail("Exception was not expected in happy scenarios");
        }

        // Minimum length: 8 characters
        try {
            String invalidPassword1 = "Lala-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // Maximum length: 32 characters
        try {
            String invalidPassword1 = "UPPER lower 12345 /`~!@#$%^&*()- 12345";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one letter in lowercase
        try {
            String invalidPassword1 = "LALALA-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one letter in uppercase
        try {
            String invalidPassword1 = "lalala-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one digit
        try {
            String invalidPassword1 = "lalala-LA";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one special character
        try {
            String invalidPassword1 = "Lalala55";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
    }

    @Test
    public void testPasswordHistoryDisabled() throws Exception {
        String username = "user1";
        String testPassword = "testPassword123.";
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(securityPolicyManager.getPasswordHistoryPolicyProperty());
            result = "0";
        }};

        securityPolicyManager.validateHistory(username, testPassword);

        new VerificationsInOrder() {{
            userPasswordHistoryDao.getPasswordHistory((User) any, anyInt);
            times = 0;
        }};
    }

    @Test(expected = DomibusCoreException.class)
    public void testValidateHistory() throws Exception {
        String username = "anyname";
        String testPassword = "anypassword";
        int oldPasswordsToCheck = 5;
        final User user = new User() {{
            setUserName(username);
            setPassword(testPassword);
        }};
        user.setDefaultPassword(true);
        List<UserPasswordHistory> oldPasswords = Arrays.asList(new UserPasswordHistory(user, testPassword, LocalDateTime.now()));

        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(securityPolicyManager.getPasswordHistoryPolicyProperty());
            result = oldPasswordsToCheck;
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.findByUserName(username);
            result = user;
            securityPolicyManager.getUserHistoryDao();
            result = userPasswordHistoryDao;
            userPasswordHistoryDao.getPasswordHistory(user, oldPasswordsToCheck);
            result = oldPasswords;
            bcryptEncoder.matches((CharSequence) any, anyString);
            result = true;
        }};

        securityPolicyManager.validateHistory(username, testPassword);
    }

    @Test
    public void testValidateDaysTillExpiration() {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final LocalDateTime passwordChangeDate = LocalDateTime.of(2018, 9, 15, 15, 58, 59);
        final Integer maxPasswordAge = 45;
        final Integer remainingDays = 15;
        final String username = "user1";
        final String maximumDefaultPasswordAgeProperty = "MaximumDefaultPasswordAgeProperty";

        new Expectations(LocalDate.class) {{
            LocalDate.now();
            result = today;
        }};
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(securityPolicyManager.getWarningDaysBeforeExpiration());
            result = "20";
            securityPolicyManager.getMaximumDefaultPasswordAgeProperty();
            result = maximumDefaultPasswordAgeProperty;
            domibusPropertyProvider.getOptionalDomainProperty(maximumDefaultPasswordAgeProperty);
            result = maxPasswordAge.toString();
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate);

        Assert.assertEquals(remainingDays, result);
    }

    @Test
    public void testValidateDaysTillExpirationDisabled() {
        final String username = "user1";
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(securityPolicyManager.getWarningDaysBeforeExpiration());
            result = "0";
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, LocalDateTime.now());
        Assert.assertEquals(null, result);
    }

    @Test(expected = CredentialsExpiredException.class)
    public void testValidatePasswordExpired() {
        final String username = "user1";
        final Integer defaultAge = 5;

        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(securityPolicyManager.getMaximumDefaultPasswordAgeProperty());
            result = defaultAge.toString();
        }};

        securityPolicyManager.validatePasswordExpired(username, true, LocalDateTime.now().minusDays(defaultAge + 1));

    }

    @Test
    public void prepareUserForUpdate() {
        final User userEntity = new User() {{
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};
        eu.domibus.api.user.User user = new eu.domibus.api.user.User() {{
            setActive(true);
        }};
        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
        }};

//        User user1 = userPersistenceService.prepareUserForUpdate(user);
//        assertNull(user1.getSuspensionDate());
//        assertEquals(0, user1.getAttemptCount(), 0d);
    }

    @Test
    public void prepareUserForUpdateSendAlert(@Mocked AccountDisabledModuleConfiguration
                                                      accountDisabledConfiguration) {
        final User userEntity = new User();
        userEntity.setActive(true);
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setActive(false);
        user.setUserName("user");
        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;

            alertConfigurationService.getAccountDisabledConfiguration();
            result = accountDisabledConfiguration;

            accountDisabledConfiguration.isActive();
            result = true;
        }};
        //userPersistenceService.prepareUserForUpdate(user);
        new Verifications() {{
//            eventService.enqueueAccountDisabledEvent(user.getUserName(), withAny(new Date()), true);
//            times = 1;
        }};
    }
}

package eu.domibus.common.validators;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.ConsoleUserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.VerificationsInOrder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class UserPasswordManagerTest {

    private static final String PASSWORD_COMPLEXITY_PATTERN = "^.*(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&+=\\-_<>.,?:;*/()|\\[\\]{}'\"\\\\]).{8,32}$";

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    UserDao userDao;

    @Injectable
    BCryptPasswordEncoder bcryptEncoder;

    @Tested
    ConsoleUserPasswordManager passwordValidator;

    @Test
    public void checkPasswordComplexity() throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(passwordValidator.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        /*
        Password should follow all of these rules:
        - Minimum length: 8 characters
        - Maximum length: 32 characters
        - At least one letter in lowercase
        - At least one letter in uppercase
        - At least one digit
        - At least one special character
         */


        // Happy Flow: No error should occur
        try {
            String testPassword1 = "Lalala-5";
            passwordValidator.validateComplexity(userName, testPassword1);

            String testPassword2 = "UPPER lower 12345 /`~!@#$%^&*()-"; // 32 characters
            passwordValidator.validateComplexity(userName, testPassword2);

            String testPassword3 = "Aa`()-_=+\\|,<.>/?;:'\"|\\[{]}.0";
            passwordValidator.validateComplexity(userName, testPassword3);

            char[] specialCharacters = new char[]{'~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '+', '=', '-', '_',
                    '|', '(', ')', '[', ']', '{', '}', '?', '/', ';', ':', ',', '.', '<', '>', '\'', '\"', '\\'};
            for (char c : specialCharacters) {
                String testPassword4 = "AlphaNum3ric " + c;
                passwordValidator.validateComplexity(userName, testPassword4);
            }

        } catch (DomibusCoreException e1) {
            Assert.fail("Exception was not expected in happy scenarios");
        }

        // Minimum length: 8 characters
        try {
            String invalidPassword1 = "Lala-5";
            passwordValidator.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // Maximum length: 32 characters
        try {
            String invalidPassword1 = "UPPER lower 12345 /`~!@#$%^&*()- 12345";
            passwordValidator.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one letter in lowercase
        try {
            String invalidPassword1 = "LALALA-5";
            passwordValidator.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one letter in uppercase
        try {
            String invalidPassword1 = "lalala-5";
            passwordValidator.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one digit
        try {
            String invalidPassword1 = "lalala-LA";
            passwordValidator.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one special character
        try {
            String invalidPassword1 = "Lalala55";
            passwordValidator.validateComplexity(userName, invalidPassword1);
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
            domibusPropertyProvider.getOptionalDomainProperty(passwordValidator.getPasswordHistoryPolicyProperty());
            result = "0";
        }};

        passwordValidator.validateHistory(username, testPassword);

        new VerificationsInOrder() {{
            userPasswordHistoryDao.getPasswordHistory((User) any, anyInt);
            times = 0;
        }};
    }

    @Test(expected = DomibusCoreException.class)
    public void testPasswordHistory() throws Exception {
        String username = "anyname";
        String testPassword = "anypassword";
        int oldPasswordsToCheck = 5;
        final User user = new User(username, testPassword);
        user.setDefaultPassword(true);
        List<UserPasswordHistory> oldPasswords = Arrays.asList(new UserPasswordHistory(user, testPassword, LocalDateTime.now()));

        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(passwordValidator.getPasswordHistoryPolicyProperty());
            result = oldPasswordsToCheck;
            userDao.loadActiveUserByUsername(username);
            result = user;
            userPasswordHistoryDao.getPasswordHistory(user, oldPasswordsToCheck);
            result = oldPasswords;
            bcryptEncoder.matches((CharSequence) any, anyString);
            result = true;
        }};

        passwordValidator.validateHistory(username, testPassword);
    }

    @Test
    public void testValidateDaysTillExpiration() {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final LocalDateTime passwordChangeDate = LocalDateTime.of(2018, 9, 15, 15, 58, 59);
        final Integer maxPasswordAge = 45;
        final Integer remainingDays = 15;
        final String username = "user1";

        new Expectations(LocalDate.class) {{
            LocalDate.now();
            result = today;
        }};
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(passwordValidator.getWarningDaysBeforeExpiration());
            result = "20";
            domibusPropertyProvider.getOptionalDomainProperty(passwordValidator.getMaximumDefaultPasswordAgeProperty());
            result = maxPasswordAge.toString();
        }};

        Integer result = passwordValidator.getDaysTillExpiration(username, true, passwordChangeDate);

        Assert.assertEquals(remainingDays, result);
    }

    @Test
    public void testValidateDaysTillExpirationDisabled() {
        final String username = "user1";
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(passwordValidator.getWarningDaysBeforeExpiration());
            result = "0";
        }};

        Integer result = passwordValidator.getDaysTillExpiration(username, true, LocalDateTime.now());
        Assert.assertEquals(null, result);
    }

    @Test(expected = CredentialsExpiredException.class)
    public void testValidatePasswordExpired() {
        final String username = "user1";
        final Integer defaultAge = 5;

        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(passwordValidator.getMaximumDefaultPasswordAgeProperty());
            result = defaultAge.toString();
        }};

        passwordValidator.validatePasswordExpired(username, true, LocalDateTime.now().minusDays(defaultAge + 1));

    }

}

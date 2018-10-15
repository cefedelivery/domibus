package eu.domibus.common.validators;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.VerificationsInOrder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;

/**
 * @since 4.1
 */

public class PasswordValidatorTest {

    private static final String PASSWORD_COMPLEXITY_PATTERN = "^.*(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&+=\\-_<>.,?:;*/()|\\[\\]{}'\"\\\\]).{8,32}$";

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    UserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    UserDao userDao;

    @Injectable
    BCryptPasswordEncoder bcryptEncoder;

    @Tested
    PasswordValidator passwordValidator;

    @Test
    public void checkPasswordComplexity() throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(PasswordValidator.PASSWORD_COMPLEXITY_PATTERN);
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
            domibusPropertyProvider.getOptionalDomainProperty(PasswordValidator.PASSWORD_HISTORY_POLICY, anyString);
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
        String username = "user1";
        String testPassword = "testPassword123.";
        List<UserPasswordHistory> oldPasswords = Arrays.asList(new UserPasswordHistory());
        new Expectations() {{
            domibusPropertyProvider.getOptionalDomainProperty(PasswordValidator.PASSWORD_HISTORY_POLICY, anyString);
            result = "5";
            userPasswordHistoryDao.getPasswordHistory((User) any, anyInt);
            result = oldPasswords;
            bcryptEncoder.matches(anyString, anyString);
            result = true;
        }};

        passwordValidator.validateHistory(username, testPassword);
    }
}

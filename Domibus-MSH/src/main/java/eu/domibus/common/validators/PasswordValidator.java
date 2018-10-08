package eu.domibus.common.validators;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Service
public class PasswordValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordValidator.class);

    protected static final String PASSWORD_COMPLEXITY_PATTERN = "domibus.passwordPolicy.pattern";
    public static final String PASSWORD_HISTORY_POLICY = "domibus.passwordPolicy.dontReuseLast";

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private UserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    protected UserDao userDao;

    public void validateComplexity(final String userName, final String password) throws DomibusCoreException {

        String errorMessage = "The password of " + userName + " user does not meet the minimum complexity requirements";
        if (StringUtils.isBlank(password)) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
        }
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        if (domain == null) {
            domain = DomainService.DEFAULT_DOMAIN;
        }
        String passwordPattern = domibusPropertyProvider.getDomainProperty(domain, PASSWORD_COMPLEXITY_PATTERN);
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
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        if (domain == null) {
            domain = DomainService.DEFAULT_DOMAIN;
        }
        int oldPasswordsToCheck;
        try {
            String oldPasswordsToCheckVal = domibusPropertyProvider.getDomainProperty(domain, PASSWORD_HISTORY_POLICY, "0");
            oldPasswordsToCheck = Integer.valueOf(oldPasswordsToCheckVal);
        } catch (NumberFormatException n) {
            oldPasswordsToCheck = 0;
        }

        if (oldPasswordsToCheck == 0) {
            return;
        }

        User user = userDao.loadActiveUserByUsername(userName);
        List<UserPasswordHistory> oldPasswords = userPasswordHistoryDao.getPasswordHistory(user, oldPasswordsToCheck);
        if (oldPasswords.stream().anyMatch(el -> bcryptEncoder.matches(password, el.getPasswordHash()))) {
            String errorMessage = "The password of " + userName + " user cannot be the same as the last " + oldPasswordsToCheck;
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
        }
    }
}

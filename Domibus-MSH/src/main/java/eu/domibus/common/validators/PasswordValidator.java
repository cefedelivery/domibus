package eu.domibus.common.validators;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 4.1
 */

@Service
public class PasswordValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordValidator.class);

    protected static final String PASSWORD_COMPLEXITY_PATTERN = "domibus.passwordPolicy.pattern";

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

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

        final String PASSWORD_COMPLEXITY_PATTERN = "^.*(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&+=\\-_<>.,?:;*/()|\\[\\]{}'\"\\\\]).{8,32}$";

        LOG.info("pattern key: \"domibus.passwordPolicy.pattern\"");
        LOG.info("pattern found    value: " + passwordPattern);
        LOG.info("pattern expected value: " + PASSWORD_COMPLEXITY_PATTERN);
        Pattern patternNoControlChar = Pattern.compile(passwordPattern);
        Matcher m = patternNoControlChar.matcher(password);
        if (!m.matches()) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
        }
    }

    public void validateHistory(final String userName, final String passwordHash) throws DomibusCoreException {

    }
}

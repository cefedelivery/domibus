package eu.domibus.ext.delegate.services.security;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.DomibusServiceException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class SecurityDefaultService implements SecurityService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityDefaultService.class);

    @Autowired
    AuthUtils authUtils;

    @Autowired
    UserMessageService userMessageService;

    @Override
    public void checkMessageAuthorization(String messageId) throws AuthenticationException, DomibusServiceException {
        /* unsecured login allowed */
        if (authUtils.isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login is allowed");
            return;
        }

        final String finalRecipient = userMessageService.getFinalRecipient(messageId);
        if (StringUtils.isEmpty(finalRecipient)) {
            throw new DomibusServiceException(DomibusErrorCode.DOM_001, "Couldn't get the finalRecipient for message with ID [" + messageId + "]");
        }
        checkAuthorization(finalRecipient);
    }

    @Override
    public void checkAuthorization(String finalRecipient) throws AuthenticationException, DomibusServiceException {
        /* unsecured login allowed */
        if (authUtils.isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login is allowed");
            return;
        }

        final String originalUserFromSecurityContext = authUtils.getOriginalUserFromSecurityContext();
        if (StringUtils.isEmpty(originalUserFromSecurityContext)) {
            LOG.debug("finalRecipient from the security context is empty, user has permission to access finalRecipient [{}]", finalRecipient);
            return;
        }

        if (StringUtils.equals(finalRecipient, originalUserFromSecurityContext)) {
            LOG.debug("The provided finalRecipient [{}] is the same as the user's finalRecipient", finalRecipient);
        } else {
            LOG.securityInfo(DomibusMessageCode.SEC_UNAUTHORIZED_MESSAGE_ACCESS, originalUserFromSecurityContext, finalRecipient);
            throw new AuthenticationException(DomibusErrorCode.DOM_002, "You are not allowed to access messages for finalRecipient [" + finalRecipient + "]. You are authorized as [" + originalUserFromSecurityContext + "]");
        }
    }

    @Override
    public String getOriginalUserFromSecurityContext() throws AuthenticationException {
        return authUtils.getOriginalUserFromSecurityContext();
    }
}

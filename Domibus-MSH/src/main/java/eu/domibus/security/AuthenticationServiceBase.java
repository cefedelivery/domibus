package eu.domibus.security;

import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.common.model.security.UserDetail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public abstract class AuthenticationServiceBase {

    @Autowired
    protected DomainService domainService;

    /**
     * Set the domain in the current security context
     *
     * @param domainCode the code of the new current domain
     */
    public void changeDomain(String domainCode) {

        if (StringUtils.isEmpty(domainCode)) {
            throw new DomainException("Could not set current domain: domain is empty");
        }
        if (!domainService.getDomains().stream().anyMatch(d -> domainCode.equalsIgnoreCase(d.getCode()))) {
            throw new DomainException("Could not set current domain: unknown domain (" + domainCode + ")");
        }

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetail securityUser = (UserDetail) authentication.getPrincipal();
        securityUser.setDomain(domainCode);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}

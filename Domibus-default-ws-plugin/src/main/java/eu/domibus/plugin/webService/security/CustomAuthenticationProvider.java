package eu.domibus.plugin.webService.security;

import eu.domibus.common.AuthRole;
import eu.domibus.plugin.webService.common.exception.AuthenticationException;
import eu.domibus.plugin.webService.common.util.HashUtil;
import eu.domibus.plugin.webService.dao.AuthenticationDAO;
import eu.domibus.plugin.webService.entity.AuthenticationEntry;
import eu.domibus.plugin.webService.impl.CustomAuthenticationInterceptor;
import eu.domibus.plugin.webService.service.IBlueCoatCertificateService;
import eu.domibus.plugin.webService.service.IX509CertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component(value="customAuthenticationProvider")
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationInterceptor.class);

    @Autowired
    private AuthenticationDAO authenticationDAO;

    @Autowired
    private IBlueCoatCertificateService blueCoatCertificateService;

    @Autowired
    private IX509CertificateService x509CertificateService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            if (authentication instanceof X509CertificateAuthentication) {
                LOG.debug("Authenticating using the X509 certificate from the request");
                authentication.setAuthenticated(x509CertificateService.isClientX509CertificateValid((X509Certificate[]) authentication.getCredentials()));

                AuthenticationEntry authenticationEntry = authenticationDAO.findByCertificateId(authentication.getName());
                ((X509CertificateAuthentication) authentication).setOriginalUser(authenticationEntry.getOriginalUser());
                List<AuthRole> authRoles = authenticationDAO.getRolesForCertificateId(authentication.getName());
                setAuthority(authentication, authRoles);
            } else if (authentication instanceof BlueCoatClientCertificateAuthentication) {
                LOG.debug("Authenticating using the decoded certificate in the http header");
                authentication.setAuthenticated(blueCoatCertificateService.isBlueCoatClientCertificateValid((CertificateDetails) authentication.getCredentials()));

                AuthenticationEntry authenticationEntry = authenticationDAO.findByCertificateId(authentication.getName());
                ((BlueCoatClientCertificateAuthentication) authentication).setOriginalUser(authenticationEntry.getOriginalUser());
                List<AuthRole> authRoles = authenticationDAO.getRolesForCertificateId(authentication.getName());
                setAuthority(authentication, authRoles);
            } else if (authentication instanceof BasicAuthentication) {
                LOG.debug("Authenticating using the Basic authentication");
                Boolean res = false;
                AuthenticationEntry basicAuthenticationEntry = authenticationDAO.findByUser(authentication.getName());
                try {
                    res = HashUtil.getSHA256Hash((String) authentication.getCredentials()).equals(basicAuthenticationEntry.getPasswd());
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
                    LOG.error("Problem hashing the provided password", ex);
                }
                authentication.setAuthenticated(res);

                ((BasicAuthentication) authentication).setOriginalUser(basicAuthenticationEntry.getOriginalUser());
                List<AuthRole> authRoles = authenticationDAO.getRolesForUser(authentication.getName());
                setAuthority(authentication, authRoles);
            }
        } catch (final Exception exception)  {
            throw new AuthenticationServiceException("Couldn't authenticate the principal " + authentication.getPrincipal(), exception);

        }
        return authentication;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return X509CertificateAuthentication.class.equals(clazz) || BlueCoatClientCertificateAuthentication.class.equals(clazz) || BasicAuthentication.class.equals(clazz);
    }

    private void setAuthority(Authentication authentication, List<AuthRole> authRoles) {
        if(authRoles == null || authRoles.isEmpty())
            return;

        List<GrantedAuthority> authorityList = new ArrayList<>();
        for(AuthRole role : authRoles) {
            authorityList.add(new SimpleGrantedAuthority(role.name()));
        }
        if(authentication instanceof BasicAuthentication) {
            ((BasicAuthentication) authentication).setAuthorityList(Collections.unmodifiableList(authorityList));
        } else if(authentication instanceof X509CertificateAuthentication) {
            ((X509CertificateAuthentication) authentication).setAuthorityList(Collections.unmodifiableList(authorityList));
        } else if(authentication instanceof BlueCoatClientCertificateAuthentication) {
            ((BlueCoatClientCertificateAuthentication) authentication).setAuthorityList(Collections.unmodifiableList(authorityList));
        }
    }
}

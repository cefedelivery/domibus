package eu.domibus.core.security;

import eu.domibus.api.security.*;
import eu.domibus.common.validators.PluginUserPasswordManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component(value="securityCustomAuthenticationProvider")
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    @Qualifier("securityAuthenticationDAO")
    private AuthenticationDAO securityAuthenticationDAO;

    @Autowired
    @Qualifier("securityBlueCoatCertificateServiceImpl")
    private BlueCoatCertificateService blueCoatCertificateService;

    @Autowired
    @Qualifier("securityX509CertificateServiceImpl")
    private X509CertificateService x509CertificateService;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    PluginUserPasswordManager pluginUserPasswordValidator;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            if (authentication instanceof X509CertificateAuthentication) {
                LOG.debug("Authenticating using the X509 certificate from the request");
                authentication.setAuthenticated(x509CertificateService.isClientX509CertificateValid((X509Certificate[]) authentication.getCredentials()));

                AuthenticationEntity authenticationEntity = securityAuthenticationDAO.findByCertificateId(authentication.getName());
                ((X509CertificateAuthentication) authentication).setOriginalUser(authenticationEntity.getOriginalUser());
                List<AuthRole> authRoles = securityAuthenticationDAO.getRolesForCertificateId(authentication.getName());
                setAuthority(authentication, authRoles);
            } else if (authentication instanceof BlueCoatClientCertificateAuthentication) {
                LOG.debug("Authenticating using the decoded certificate in the http header");
                authentication.setAuthenticated(blueCoatCertificateService.isBlueCoatClientCertificateValid((CertificateDetails) authentication.getCredentials()));

                AuthenticationEntity authenticationEntity = securityAuthenticationDAO.findByCertificateId(authentication.getName());
                ((BlueCoatClientCertificateAuthentication) authentication).setOriginalUser(authenticationEntity.getOriginalUser());
                List<AuthRole> authRoles = securityAuthenticationDAO.getRolesForCertificateId(authentication.getName());
                setAuthority(authentication, authRoles);
            } else if (authentication instanceof BasicAuthentication) {
                LOG.debug("Authenticating using the Basic authentication");

                AuthenticationEntity userEntity = securityAuthenticationDAO.findByUser(authentication.getName());
                //check if password is correct
                boolean isPasswordCorrect = bcryptEncoder.matches((String) authentication.getCredentials(), userEntity.getPassword());
                //check if password expired
                boolean isPasswordExpired = isPasswordExpired(userEntity);

                authentication.setAuthenticated(isPasswordCorrect && !isPasswordExpired);

                ((BasicAuthentication) authentication).setOriginalUser(userEntity.getOriginalUser());
                List<AuthRole> authRoles = securityAuthenticationDAO.getRolesForUser(authentication.getName());
                setAuthority(authentication, authRoles);
            }
        } catch (final Exception exception)  {
            throw new AuthenticationServiceException("Couldn't authenticate the principal " + authentication.getPrincipal(), exception);
        }
        return authentication;
    }

    public boolean isPasswordExpired(AuthenticationEntity user) {
        boolean isDefaultPassword = user.hasDefaultPassword();
        LocalDateTime passwordChangeDate = user.getPasswordChangeDate();
        try {
            pluginUserPasswordValidator.validatePasswordExpired(user.getUsername(), isDefaultPassword, passwordChangeDate);
            return false;
        } catch (CredentialsExpiredException ex) {
            return true;
        }
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

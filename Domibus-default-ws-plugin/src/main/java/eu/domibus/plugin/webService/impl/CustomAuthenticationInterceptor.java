package eu.domibus.plugin.webService.impl;

import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.plugin.webService.common.exception.AuthenticationException;
import eu.domibus.plugin.webService.security.BasicAuthentication;
import eu.domibus.plugin.webService.security.BlueCoatClientCertificateAuthentication;
import eu.domibus.plugin.webService.security.CustomAuthenticationProvider;
import eu.domibus.plugin.webService.security.X509CertificateAuthentication;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

import java.security.cert.X509Certificate;
import java.util.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component(value = "customAuthenticationInterceptor")
public class CustomAuthenticationInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CustomAuthenticationInterceptor.class);

    private static final String BASIC_HEADER_KEY = "Authorization";
    private static final String CLIENT_CERT_ATTRIBUTE_KEY = "javax.servlet.request.X509Certificate";
    private static final String CLIENT_CERT_HEADER_KEY = "Client-Cert";
    private static final String UNSECURE_LOGIN_ALLOWED = "domibus.auth.unsecureLoginAllowed";

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Autowired
    private CustomAuthenticationProvider authenticationProvider;

    public CustomAuthenticationInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        HttpServletRequest httpRequest = (HttpServletRequest) message.get("HTTP.REQUEST");

        LOGGER.debug("Intercepted request for " + httpRequest.getPathInfo());

        /* id domibus allows unsecure login, do not authenticate anymore, just go on */
        if ("true".equals(domibusProperties.getProperty(UNSECURE_LOGIN_ALLOWED, "true"))) {
            LOGGER.businessInfo(DomibusMessageCode.SEC_UNSECURED_LOGIN_ALLOWED);
            return;
        }

        final Object certificateAttribute = httpRequest.getAttribute(CLIENT_CERT_ATTRIBUTE_KEY);
        final String certHeaderValue = httpRequest.getHeader(CLIENT_CERT_HEADER_KEY);
        final String basicHeaderValue = httpRequest.getHeader(BASIC_HEADER_KEY);

        if (basicHeaderValue != null) {
            LOGGER.debug("Basic authentication header found: " + basicHeaderValue);
        }
        if (certificateAttribute != null) {
            LOGGER.debug("CertificateAttribute found: " + certificateAttribute.getClass());
        }
        if (certHeaderValue != null) {
            LOGGER.debug("Client certificate in header found: " + certHeaderValue);
        }

        try {
            if (basicHeaderValue != null && basicHeaderValue.startsWith("Basic")) {
                LOGGER.securityInfo(DomibusMessageCode.SEC_BASIC_AUTHENTICATION_USE);

                LOGGER.debug("Basic authentication: " + Base64.decode(basicHeaderValue.substring("Basic ".length())));
                String basicAuthCredentials = new String(Base64.decode(basicHeaderValue.substring("Basic ".length())));
                int index = basicAuthCredentials.indexOf(":");
                String user = basicAuthCredentials.substring(0, index);
                String password = basicAuthCredentials.substring(index + 1);
                BasicAuthentication authentication = new BasicAuthentication(user, password);
                authenticate(authentication, httpRequest);
            } else if ("https".equalsIgnoreCase(httpRequest.getScheme())) {
                if (certificateAttribute == null) {
                    throw new AuthenticationException("No client certificate present in the request");
                }
                if (!(certificateAttribute instanceof X509Certificate[])) {
                    throw new AuthenticationException("Request value is not of type X509Certificate[] but of " + certificateAttribute.getClass());
                }
                LOGGER.securityInfo(DomibusMessageCode.SEC_X509CERTIFICATE_AUTHENTICATION_USE);
                final X509Certificate[] certificates = (X509Certificate[]) certificateAttribute;
                X509CertificateAuthentication authentication = new X509CertificateAuthentication(certificates);
                authenticate(authentication, httpRequest);
            } else if ("http".equalsIgnoreCase(httpRequest.getScheme())) {
                if (certHeaderValue == null) {
                    throw new AuthenticationException("There is no valid authentication in this request and unsecure login is not allowed.");
                }
                LOGGER.securityInfo(DomibusMessageCode.SEC_BLUE_COAT_AUTHENTICATION_USE);
                Authentication authentication = new BlueCoatClientCertificateAuthentication(certHeaderValue);
                authenticate(authentication, httpRequest);
            } else {
                throw new AuthenticationException("There is no valid authentication in this request and unsecure login is not allowed.");
            }
        } catch (AuthenticationException e) {
            LOGGER.error("Error performing authentication", e);
            throw new Fault(e);
        }
    }

    private void authenticate(Authentication authentication, HttpServletRequest httpRequest) throws AuthenticationException {
        LOGGER.securityInfo(DomibusMessageCode.SEC_CONNECTION_ATTEMPT, httpRequest.getRemoteHost(), httpRequest.getRequestURL());
        Authentication authenticationResult;
        try {
            authenticationResult = authenticationProvider.authenticate(authentication);
        } catch (org.springframework.security.core.AuthenticationException exc) {
            throw new AuthenticationException("Error while authenticating " + authentication.getName(), exc);
        }

        if (authenticationResult.isAuthenticated()) {
            LOGGER.securityInfo(DomibusMessageCode.SEC_AUTHORIZED_ACCESS, httpRequest.getRemoteHost(), httpRequest.getRequestURL(), authenticationResult.getAuthorities());
            LOGGER.debug("Request authenticated. Storing the authentication result in the security context");
            LOGGER.debug("Authentication result: " + authenticationResult);
            SecurityContextHolder.getContext().setAuthentication(authenticationResult);
            LOGGER.putMDC(DomibusLogger.MDC_USER, authenticationResult.getName());
        } else {
            LOGGER.securityInfo(DomibusMessageCode.SEC_UNAUTHORIZED_ACCESS, httpRequest.getRemoteHost(), httpRequest.getRequestURL());
            LOGGER.debug("Unauthorize access for " + httpRequest.getRemoteHost() + " " + httpRequest.getRequestURL());
            throw new AuthenticationException("The certificate is not valid or is not present or the basic authentication credentials are invalid");
        }
    }
}

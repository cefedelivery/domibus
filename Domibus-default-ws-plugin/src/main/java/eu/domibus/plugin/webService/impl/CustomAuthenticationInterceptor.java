package eu.domibus.plugin.webService.impl;

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

@Component(value="customAuthenticationInterceptor")
public class CustomAuthenticationInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomAuthenticationInterceptor.class);
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

        LOG.debug("Intercepted request for " + httpRequest.getPathInfo());

        /* id domibus allows unsecure login, do not authenticate anymore, just go on */
        if("true".equals(domibusProperties.getProperty(UNSECURE_LOGIN_ALLOWED, "true"))) {
            LOG.debug("Unsecure login is allowed, no authentication will be performed");
            return;
        }

        final Object certificateAttribute = httpRequest.getAttribute(CLIENT_CERT_ATTRIBUTE_KEY);
        final String certHeaderValue = httpRequest.getHeader(CLIENT_CERT_HEADER_KEY);
        final String basicHeaderValue = httpRequest.getHeader(BASIC_HEADER_KEY);

        if(basicHeaderValue != null) {
            LOG.debug("Basic authentication header found: " + basicHeaderValue);
        }
        if (certificateAttribute != null) {
            LOG.debug("CertificateAttribute found: " + certificateAttribute.getClass());
        }
        if (certHeaderValue != null) {
            LOG.debug("Client certificate in header found: " + certHeaderValue);
        }

        try {
            if (basicHeaderValue != null && basicHeaderValue.startsWith("Basic")) {
                LOG.debug("Basic authentication: " + Base64.decode(basicHeaderValue.substring("Basic ".length())));
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
                LOG.debug("X509Certificates authentication found");
                final X509Certificate[] certificates = (X509Certificate[]) certificateAttribute;
                X509CertificateAuthentication authentication = new X509CertificateAuthentication(certificates);
                authenticate(authentication, httpRequest);
            } else if ("http".equalsIgnoreCase(httpRequest.getScheme())) {
                if (certHeaderValue == null) {
                    throw new AuthenticationException("There is no valid authentication in this request and unsecure login is not allowed.");
                }
                Authentication authentication = new BlueCoatClientCertificateAuthentication(certHeaderValue);
                authenticate(authentication, httpRequest);
            } else {
                throw new AuthenticationException("There is no valid authentication in this request and unsecure login is not allowed.");
            }
        }catch (AuthenticationException e) {
            throw new Fault(e);
        }
    }

    private void authenticate(Authentication authentication, HttpServletRequest httpRequest) throws AuthenticationException {
        Authentication authenticationResult;
        try {
            authenticationResult = authenticationProvider.authenticate(authentication);
        } catch (org.springframework.security.core.AuthenticationException exc) {
            throw new AuthenticationException("Error while authenticating " + authentication.getName(), exc);
        }

        if (authenticationResult.isAuthenticated()) {
            LOG.debug("Request authenticated. Storing the authentication result in the security context");
            LOG.debug("Authentication result: " + authenticationResult);
            SecurityContextHolder.getContext().setAuthentication(authenticationResult);
        } else {
            LOG.debug("Unauthorize access for " +  httpRequest.getRemoteHost() + " " + httpRequest.getRequestURL().toString());
            throw new AuthenticationException("The certificate is not valid or is not present or the basic authentication credentials are invalid");
        }
    }
}

package eu.domibus.core.security;

import eu.domibus.api.security.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateServiceImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class AuthenticationDefaultServiceTest {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationDefaultServiceTest.class);

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String TEST_KEYSTORE = "testkeystore.jks";
    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String TEST_KEYSTORE_PASSWORD = "1234";

    @Injectable
    private AuthenticationProvider securityCustomAuthenticationProvider;

    private MockHttpServletRequest request;

    private CertificateServiceImpl certificateService = new CertificateServiceImpl();

    @Injectable
    private Properties domibusProperties;

    @Tested
    AuthenticationService authenticationService = new AuthenticationDefaultService();

    @Test
    public void authenticateX509Test() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );
        request.setScheme("https");

        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);

        MockHttpServletRequest postProcessedRequest = x509(certificate).postProcessRequest(request);

        X509Certificate[] certificates = (X509Certificate[]) postProcessedRequest
                .getAttribute("javax.servlet.request.X509Certificate");

        new Expectations() {{
            domibusProperties.getProperty(AuthenticationDefaultService.UNSECURE_LOGIN_ALLOWED, "true");
            result = false;
            Authentication authentication = new X509CertificateAuthentication(certificates);
            authentication.setAuthenticated(true);
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            result = authentication;
        }};

        authenticationService.authenticate(postProcessedRequest);
    }


    @Test(expected = AuthenticationException.class)
    public void authenticateX509MissingTest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );
        request.setScheme("https");

        authenticationService.authenticate(request);

        new Verifications() {{
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            times = 0;
        }};
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateX509InvalidTest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );
        request.setScheme("https");

        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);

        MockHttpServletRequest postProcessedRequest = x509(certificate).postProcessRequest(request);

        X509Certificate[] certificates = (X509Certificate[]) postProcessedRequest
                .getAttribute("javax.servlet.request.X509Certificate");

        new Expectations() {{
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            result = new AuthenticationCredentialsNotFoundException(anyString);
        }};

        authenticationService.authenticate(postProcessedRequest);

        new Verifications() {{
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            times = 1;
        }};

    }
    @Test
    public void authenticateDisabledTest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );
        request.setScheme("https");

        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);

        MockHttpServletRequest postProcessedRequest = x509(certificate).postProcessRequest(request);

        X509Certificate[] certificates = (X509Certificate[]) postProcessedRequest
                .getAttribute("javax.servlet.request.X509Certificate");

        new Expectations() {{
            domibusProperties.getProperty(AuthenticationDefaultService.UNSECURE_LOGIN_ALLOWED, "true");
            result = true;
        }};

        authenticationService.authenticate(postProcessedRequest);

        new Verifications() {{
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            times = 0;
        }};
    }

    @Test
    public void authenticateBasicValidTest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );
        request.addHeader(AuthenticationDefaultService.BASIC_HEADER_KEY, "Basic YWRtaW46MTIzNDU2");

        new Expectations() {{
            Authentication authentication = new BasicAuthentication(AuthRole.ROLE_USER);
            authentication.setAuthenticated(true);
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            result = authentication;
        }};

        authenticationService.authenticate(request);

        new Verifications() {{
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            times = 1;
        }};
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateBasicInvalidTest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );
        request.addHeader(AuthenticationDefaultService.BASIC_HEADER_KEY, "Basic YWRtaW46MTIzNDU2");

        new Expectations() {{
            Authentication authentication = new BasicAuthentication(AuthRole.ROLE_USER);
            authentication.setAuthenticated(false);
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            result = authentication;
        }};

        authenticationService.authenticate(request);

        new Verifications() {{
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            times = 1;
        }};
    }

    @Test
    public void authenticateBLueCoatValidTest() throws UnsupportedEncodingException, ParseException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );

        String serial = "123ABCD";
        String issuer = "CN=PEPPOL SERVICE METADATA PUBLISHER TEST CA,OU=FOR TEST PURPOSES ONLY,O=NATIONAL IT AND TELECOM AGENCY,C=DK";
        String subject = "O=DG-DIGIT,CN=SMP_1000000007,C=BE";
        DateFormat df = new SimpleDateFormat("MMM d hh:mm:ss yyyy zzz", Locale.US);
        Date validFrom = df.parse("Jun 01 10:37:53 2015 CEST");
        Date validTo = df.parse("Jun 01 10:37:53 2035 CEST");

        String certHeaderValue = "serial=" + serial + "&subject=" + subject + "&validFrom="+ df.format(validFrom) +"&validTo=" + df.format(validTo) +"&issuer=" + issuer;

        request.addHeader(AuthenticationDefaultService.CLIENT_CERT_HEADER_KEY, certHeaderValue);

        new Expectations() {{
            Authentication authentication = new BlueCoatClientCertificateAuthentication(certHeaderValue);
            authentication.setAuthenticated(true);
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            result = authentication;
        }};

        authenticationService.authenticate(request);

        new Verifications() {{
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            times = 1;
        }};
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateEnabledMissingTest() throws UnsupportedEncodingException, ParseException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );

        new Expectations() {{
            domibusProperties.getProperty(AuthenticationDefaultService.UNSECURE_LOGIN_ALLOWED, "true");
            result = false;
        }};

        authenticationService.authenticate(request);

        new Verifications() {{
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            times = 0;
        }};
    }
}

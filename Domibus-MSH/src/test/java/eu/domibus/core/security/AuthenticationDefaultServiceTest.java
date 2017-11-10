package eu.domibus.core.security;

import eu.domibus.api.security.AuthenticationService;
import eu.domibus.api.security.X509CertificateAuthentication;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateServiceImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import java.security.cert.X509Certificate;
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
    public void authenticateTest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "https://localhost:8080/domibus/services/backend" );
        request.setScheme("https");

        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);

        MockHttpServletRequest postProcessedRequest = x509(certificate).postProcessRequest(request);

        X509Certificate[] certificates = (X509Certificate[]) postProcessedRequest
                .getAttribute("javax.servlet.request.X509Certificate");

        new Expectations() {{

//            domibusProperties.getProperty(NotificationListenerService.PROP_LIST_PENDING_MESSAGES_MAXCOUNT, "500");
//            result = 5;

            Authentication authentication = new X509CertificateAuthentication(certificates);
            authentication.setAuthenticated(true);
            securityCustomAuthenticationProvider.authenticate((Authentication)any);
            result = authentication;

        }};


        authenticationService.authenticate(postProcessedRequest);
    }

}

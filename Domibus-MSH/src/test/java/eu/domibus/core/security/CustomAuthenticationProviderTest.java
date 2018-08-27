package eu.domibus.core.security;

import eu.domibus.api.security.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateServiceImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertNotNull;

/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class CustomAuthenticationProviderTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomAuthenticationProviderTest.class);

    private CertificateServiceImpl certificateService = new CertificateServiceImpl();

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String TEST_KEYSTORE = "testkeystore.jks";
    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String TEST_KEYSTORE_PASSWORD = "1234";

    @Injectable
    private AuthenticationDAO securityAuthenticationDAO;

    @Injectable
    private BlueCoatCertificateService securityBlueCoatCertificateServiceImpl;

    @Injectable
    private X509CertificateService securityX509CertificateServiceImpl;

    @Tested
    CustomAuthenticationProvider securityCustomAuthenticationProvider;

    @Injectable
    BCryptPasswordEncoder bcryptEncoder;

    @Test
    public void authenticateX509Test() {
        Authentication authentication = createX509Auth();

        new Expectations() {{
            securityAuthenticationDAO.getRolesForCertificateId(anyString);
            result = createAuthRoles();;
        }};

        securityCustomAuthenticationProvider.authenticate(authentication);
    }

    @Test
    public void authenticateBlueCoatTest()  throws UnsupportedEncodingException, ParseException {
        Authentication authentication = createBlueCoatAuth();

        new Expectations() {{
            securityAuthenticationDAO.getRolesForCertificateId(anyString);
            result = createAuthRoles();;
        }};

        securityCustomAuthenticationProvider.authenticate(authentication);
    }

    @Test
    public void authenticateBasicTest()  throws UnsupportedEncodingException, ParseException {
        Authentication authentication = new BasicAuthentication("admin", "123456");

        new Expectations() {{
            AuthenticationEntity basicAuthenticationEntity = new AuthenticationEntity();
            basicAuthenticationEntity.setAuthRoles("ROLE_ADMIN");
            basicAuthenticationEntity.setUsername("admin");
            basicAuthenticationEntity.setPasswd("123456");

            securityAuthenticationDAO.findByUser(anyString);
            result = basicAuthenticationEntity;

            securityAuthenticationDAO.getRolesForUser(anyString);
            result = createAuthRoles();;
        }};

        securityCustomAuthenticationProvider.authenticate(authentication);
    }

    private List<AuthRole> createAuthRoles() {
        List<AuthRole> authRoles = new ArrayList<>();
        authRoles.add(AuthRole.ROLE_ADMIN);
        authRoles.add(AuthRole.ROLE_USER);
        return authRoles;
    }

    private Authentication createX509Auth() {
        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);
        X509Certificate[] certificates = new X509Certificate[1];
        certificates[0] = certificate;

        Authentication authentication = new X509CertificateAuthentication(certificates);
        return authentication;
    }

    private Authentication createBlueCoatAuth() throws ParseException {
        String serial = "123ABCD";
        String issuer = "CN=PEPPOL SERVICE METADATA PUBLISHER TEST CA,OU=FOR TEST PURPOSES ONLY,O=NATIONAL IT AND TELECOM AGENCY,C=DK";
        String subject = "O=DG-DIGIT,CN=SMP_1000000007,C=BE";
        DateFormat df = new SimpleDateFormat("MMM d hh:mm:ss yyyy zzz", Locale.US);
        Date validFrom = df.parse("Jun 01 10:37:53 2015 CEST");
        Date validTo = df.parse("Jun 01 10:37:53 2035 CEST");
        String certHeaderValue = "serial=" + serial + "&subject=" + subject + "&validFrom="+ df.format(validFrom) +"&validTo=" + df.format(validTo) +"&issuer=" + issuer;

        return new BlueCoatClientCertificateAuthentication(certHeaderValue);
    }
}

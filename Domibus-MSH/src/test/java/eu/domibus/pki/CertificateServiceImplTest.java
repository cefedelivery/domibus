package eu.domibus.pki;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.Security;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertFalse;

/**
 * Created by Cosmin Baciu on 07-Jul-16.
 */
@RunWith(JMockit.class)
public class CertificateServiceImplTest {

    @Tested
    CertificateServiceImpl certificateService;

    @Injectable
    X509Certificate x509Certificate;

    @Before
    public void init() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Test
    public void testIsCertificateValidWithExpiredCertificate() throws Exception {
        new Expectations() {{
            x509Certificate.getNotBefore();
            result = new DateTime().minusDays(2).toDate();
            x509Certificate.getNotAfter();
            result = new DateTime().minusDays(1).toDate();
        }};

        boolean certificateValid = certificateService.isCertificateValid(x509Certificate);
        assertFalse(certificateValid);
    }

    @Test
    public void testIsCertificateValidWithNotYetValidCertificate() throws Exception {
        new Expectations() {{
            x509Certificate.getNotBefore();
            result = new DateTime().plusDays(2).toDate();
            x509Certificate.getNotAfter();
            result = new DateTime().plusDays(5).toDate();
        }};

        boolean certificateValid = certificateService.isCertificateValid(x509Certificate);
        assertFalse(certificateValid);
    }

}

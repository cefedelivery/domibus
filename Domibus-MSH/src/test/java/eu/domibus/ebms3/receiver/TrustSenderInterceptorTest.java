package eu.domibus.ebms3.receiver;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Properties;

/**
 * @author idragusa
 * @since 3.3
 */
@RunWith(JMockit.class)
public class TrustSenderInterceptorTest {

    private static final Log LOG = LogFactory.getLog(TrustSenderInterceptorTest.class);

    @Injectable
    Properties domibusProperties;

    @Tested
    TrustSenderInterceptor trustSenderInterceptor;

    @Test
    public void testSenderTrustVerificationPropertyFalse() {
        Assert.assertFalse(trustSenderInterceptor.isInterceptorEnabled());
    }

    @Test
    public void testSenderTrustVerificationPropertyTrue() {
        new Expectations() {{
            domibusProperties.getProperty(TrustSenderInterceptor.DOMIBUS_SENDERPARTY_TRUST_VERIFICATION, "false");
            result = "true";
        }};

        Assert.assertTrue(trustSenderInterceptor.isInterceptorEnabled());
    }
}
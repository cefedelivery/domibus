package eu.domibus.plugin.webService;

import eu.domibus.AbstractIT;
import eu.domibus.ebms3.receiver.MessageLegConfigurationFactory;
import eu.domibus.ebms3.receiver.SetPolicyInInterceptor;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;


/**
 *
 * @author draguio
 * @since 3.3
 */
@ContextConfiguration("classpath:pmode-dao.xml")
public class SetPolicyInInterceptorIT extends AbstractIT {

    private static boolean initialized;

    @Autowired
    SetPolicyInInterceptor setPolicyInInterceptor;

    @Autowired
    MessageLegConfigurationFactory serverInMessageLegConfigurationFactory;
    @Before
    public void before() throws IOException {

        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageDataset.sql");
            initialized = true;
        }
        setPolicyInInterceptor.setMessageLegConfigurationFactory(serverInMessageLegConfigurationFactory);
    }

    @Test
    public void testHandleMessage() {
        String expectedPolicy = "doNothingPolicy";
        String expectedSecurityAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

        String filename = "SOAPMessage.xml";
        SoapMessage sm = createSoapMessage(filename);

        setPolicyInInterceptor.handleMessage(sm);

        Assert.assertEquals(expectedPolicy, ((Policy)sm.get(PolicyConstants.POLICY_OVERRIDE)).getId());
        Assert.assertEquals(expectedSecurityAlgorithm, sm.get(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM));
    }

    @Test(expected = org.apache.cxf.interceptor.Fault.class)
    public void testHandleMessageNull() {

        String filename = "SOAPMessageNoMessaging.xml";
        SoapMessage sm = createSoapMessage(filename);

        // handle message without adding any content
        setPolicyInInterceptor.handleMessage(sm);
    }
}

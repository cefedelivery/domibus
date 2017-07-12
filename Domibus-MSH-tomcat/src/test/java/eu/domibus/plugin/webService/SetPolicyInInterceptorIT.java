package eu.domibus.plugin.webService;

import eu.domibus.AbstractIT;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.receiver.MessageLegConfigurationFactory;
import eu.domibus.ebms3.receiver.SetPolicyInInterceptor;
import eu.domibus.ebms3.sender.MSHDispatcher;
import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;


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

    private SoapMessage createSoapMessage(String dataset) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/as4/" + dataset);

        SoapMessage sm = new SoapMessage(new MessageImpl());
        sm.setContent(InputStream.class, is);
        InterceptorChain ic = new PhaseInterceptorChain((new PhaseManagerImpl()).getOutPhases());
        sm.setInterceptorChain(ic);
        ExchangeImpl exchange = new ExchangeImpl();
        Bus bus = new ExtensionManagerBus();
        bus.setExtension(new PolicyBuilderImpl(bus), PolicyBuilder.class);
        exchange.put(Bus.class, bus);
        sm.setExchange(exchange);

        return sm;
    }
}

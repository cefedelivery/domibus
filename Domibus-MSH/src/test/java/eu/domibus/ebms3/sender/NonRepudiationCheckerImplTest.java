package eu.domibus.ebms3.sender;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.common.model.NonRepudiationConstants;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.dom.WSConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.NodeList;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 3.3.1
 */
@RunWith(JMockit.class)
public class NonRepudiationCheckerImplTest {

    @Tested
    NonRepudiationCheckerImpl nonRepudiationChecker;

    @Test
    public void testGetNonRepudiationNodeListFromRequest() throws Exception {
        final NodeList referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest();
        assertEquals(referencesFromSecurityHeader.getLength(), 6);
    }

    protected NodeList getNonRepudiationNodeListFromRequest() throws SOAPException, IOException, EbMS3Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage(null, new ClassPathResource("dataset/as4/MSHAS4Request.xml").getInputStream());
        return nonRepudiationChecker.getNonRepudiationNodeList(request.getSOAPHeader().getElementsByTagNameNS(WSConstants.SIG_NS, WSConstants.SIG_LN).item(0));
    }

    @Test
    public void testGetNonRepudiationNodeListFromResponse() throws Exception {
        final NodeList referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse();
        assertEquals(referencesFromNonRepudiationInformation.getLength(), 6);
    }

    protected NodeList getNonRepudiationListFromResponse() throws SOAPException, IOException, EbMS3Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage response = messageFactory.createMessage(null, new ClassPathResource("dataset/as4/MSHAS4Response.xml").getInputStream());
        return nonRepudiationChecker.getNonRepudiationNodeList(response.getSOAPHeader().getElementsByTagNameNS(NonRepudiationConstants.NS_NRR, NonRepudiationConstants.NRR_LN).item(0));
    }

    @Test
    public void compareUnorderedReferenceNodeLists() throws Exception {
        final NodeList referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest();
        final NodeList referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse();
        final boolean compareUnorderedReferenceNodeListsResult = nonRepudiationChecker.compareUnorderedReferenceNodeLists(referencesFromSecurityHeader, referencesFromNonRepudiationInformation);
        Assert.assertTrue(compareUnorderedReferenceNodeListsResult);
    }

}
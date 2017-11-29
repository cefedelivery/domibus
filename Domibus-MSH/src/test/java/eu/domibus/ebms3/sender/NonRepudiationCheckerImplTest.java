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
        final NodeList referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest("dataset/as4/MSHAS4Request.xml");
        assertEquals(referencesFromSecurityHeader.getLength(), 6);
    }

    @Test
    public void testGetNonRepudiationNodeListFromResponse() throws Exception {
        final NodeList referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse("dataset/as4/MSHAS4Response.xml");
        assertEquals(referencesFromNonRepudiationInformation.getLength(), 6);
    }

    @Test
    public void testGetNonRepudiationNodeListFromRequestSignOnly() throws Exception {
        final NodeList referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest("dataset/as4/MSHAS4Request-signOnly.xml");
        assertEquals(referencesFromSecurityHeader.getLength(), 6);
    }

    @Test
    public void testGetNonRepudiationNodeListFromResponseSignOnly() throws Exception {
        final NodeList referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse("dataset/as4/MSHAS4Response-signOnly.xml");
        assertEquals(referencesFromNonRepudiationInformation.getLength(), 6);
    }

    @Test
    public void compareUnorderedReferenceNodeLists() throws Exception {
        final NodeList referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest("dataset/as4/MSHAS4Request.xml");
        final NodeList referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse("dataset/as4/MSHAS4Response.xml");
        final boolean compareUnorderedReferenceNodeListsResult = nonRepudiationChecker.compareUnorderedReferenceNodeLists(referencesFromSecurityHeader, referencesFromNonRepudiationInformation);
        Assert.assertTrue(compareUnorderedReferenceNodeListsResult);
    }

    @Test
    public void compareUnorderedReferenceNodeListsSignOnly() throws Exception {
        final NodeList referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest("dataset/as4/MSHAS4Request-signOnly.xml");
        final NodeList referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse("dataset/as4/MSHAS4Response-signOnly.xml");
        final boolean compareUnorderedReferenceNodeListsResult = nonRepudiationChecker.compareUnorderedReferenceNodeLists(referencesFromSecurityHeader, referencesFromNonRepudiationInformation);
        Assert.assertTrue(compareUnorderedReferenceNodeListsResult);
    }

    protected NodeList getNonRepudiationNodeListFromRequest(String path) throws SOAPException, IOException, EbMS3Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage(null, new ClassPathResource(path).getInputStream());
        return nonRepudiationChecker.getNonRepudiationNodeList(request.getSOAPHeader().getElementsByTagNameNS(WSConstants.SIG_NS, WSConstants.SIG_INFO_LN).item(0));
    }

    protected NodeList getNonRepudiationListFromResponse(String path) throws SOAPException, IOException, EbMS3Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage response = messageFactory.createMessage(null, new ClassPathResource(path).getInputStream());
        return nonRepudiationChecker.getNonRepudiationNodeList(response.getSOAPHeader().getElementsByTagNameNS(NonRepudiationConstants.NS_NRR, NonRepudiationConstants.NRR_LN).item(0));
    }

}
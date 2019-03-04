package eu.domibus.ebms3.sender;

import eu.domibus.ebms3.common.model.NonRepudiationConstants;
import eu.domibus.util.SoapUtil;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.wss4j.dom.WSConstants;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.NodeList;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.3.1
 */
@RunWith(JMockit.class)
public class NonRepudiationCheckerImplTest {

    NonRepudiationCheckerImpl nonRepudiationChecker = new NonRepudiationCheckerImpl();

    static MessageFactory messageFactory = null;

    @BeforeClass
    public static void init() throws SOAPException {
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    }

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

    protected NodeList getNonRepudiationNodeListFromRequest(String path) throws Exception {
        SOAPMessage request = new SoapUtil().createSOAPMessage(IOUtils.toString(new ClassPathResource(path).getInputStream()));
        return nonRepudiationChecker.getNonRepudiationNodeList(request.getSOAPHeader().getElementsByTagNameNS(WSConstants.SIG_NS, WSConstants.SIG_INFO_LN).item(0));
    }

    protected NodeList getNonRepudiationListFromResponse(String path) throws Exception {
        SOAPMessage response = new SoapUtil().createSOAPMessage(IOUtils.toString(new ClassPathResource(path).getInputStream()));
        return nonRepudiationChecker.getNonRepudiationNodeList(response.getSOAPHeader().getElementsByTagNameNS(NonRepudiationConstants.NS_NRR, NonRepudiationConstants.NRR_LN).item(0));
    }

}
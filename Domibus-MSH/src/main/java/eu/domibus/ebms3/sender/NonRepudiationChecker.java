package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * Created by feriaad on 15/02/2016.
 */
@Service
public class NonRepudiationChecker {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NonRepudiationChecker.class);
    private static final String XPATH_EXPRESSION_STRING = "/*/*/*[local-name() = 'Reference']/@URI  | /*/*/*[local-name() = 'Reference']/*[local-name() = 'DigestValue']";
    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    public NodeList getNonRepudiationNodeList(final Node securityInfo) throws EbMS3Exception {
        NodeList nodes = null;
        try {
            nodes = (NodeList) NonRepudiationChecker.xPath.evaluate(NonRepudiationChecker.XPATH_EXPRESSION_STRING, securityInfo, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            assert false;
            // due to the fact that we use a static expression this can never occur.
            throw new RuntimeException(e);
        }

        if (nodes == null) {
            EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "No Reference Data found in either security header or nonrepudiationinformation", null, null);
            e.setMshRole(MSHRole.SENDING);
            throw e;
        }

        return nodes;
    }

    public boolean compareUnorderedReferenceNodeLists(final NodeList referencesFromSecurityHeader, final NodeList referencesFromNonRepudiationInformation) {
        if (referencesFromSecurityHeader.getLength() != referencesFromNonRepudiationInformation.getLength()) {
            return false;
        }
        boolean found = false;
        for (int i = 0; i < referencesFromSecurityHeader.getLength(); ++i) {
            for (int j = 0; j < referencesFromNonRepudiationInformation.getLength(); ++j) {
                if (referencesFromSecurityHeader.item(i).getTextContent().equals(referencesFromNonRepudiationInformation.item(j).getTextContent())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
            found = false;
        }

        return true;
    }
}

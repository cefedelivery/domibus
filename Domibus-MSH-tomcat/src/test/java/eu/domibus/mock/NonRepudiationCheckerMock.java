package eu.domibus.mock;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.sender.NonRepudiationChecker;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service("NonRepudiationCheckerMock")
@Primary
public class NonRepudiationCheckerMock implements NonRepudiationChecker {

    @Override
    public NodeList getNonRepudiationNodeList(Node securityInfo) throws EbMS3Exception {
        return null;
    }

    @Override
    public boolean compareUnorderedReferenceNodeLists(NodeList referencesFromSecurityHeader, NodeList referencesFromNonRepudiationInformation) {
        return true;
    }
}

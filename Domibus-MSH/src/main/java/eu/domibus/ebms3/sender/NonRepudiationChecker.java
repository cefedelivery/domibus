package eu.domibus.ebms3.sender;

import eu.domibus.common.exception.EbMS3Exception;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface NonRepudiationChecker {

    NodeList getNonRepudiationNodeList(Node securityInfo) throws EbMS3Exception;

    boolean compareUnorderedReferenceNodeLists(NodeList referencesFromSecurityHeader, NodeList referencesFromNonRepudiationInformation);
}

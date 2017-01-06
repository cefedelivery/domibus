/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class ReliabilityChecker {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ReliabilityChecker.class);

    @Autowired
    @Qualifier("jaxbContextEBMS")
    JAXBContext jaxbContext;

    @Autowired
    private NonRepudiationChecker nonRepudiationChecker;

    @Autowired
    private PModeProvider pModeProvider;

    public CheckResult check(final SOAPMessage request, final SOAPMessage response, final String pmodeKey) throws EbMS3Exception {

        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        String messageId = null;

        if (legConfiguration.getReliability() != null && ReplyPattern.CALLBACK.equals(legConfiguration.getReliability().getReplyPattern())) {
            LOG.debug("Reply pattern is waiting for callback, setting message status to WAITING_FOR_CALLBACK.");
            return CheckResult.WAITING_FOR_CALLBACK;
        }

        if (legConfiguration.getReliability() != null && ReplyPattern.RESPONSE.equals(legConfiguration.getReliability().getReplyPattern())) {
            LOG.debug("Checking reliability for outgoing message");
            final Messaging messaging;

            try {
                messaging = this.jaxbContext.createUnmarshaller().unmarshal((Node) response.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next(), Messaging.class).getValue();
            } catch (JAXBException | SOAPException e) {
                LOG.error(e.getMessage(), e);
                return CheckResult.FAIL;
            }

            final SignalMessage signalMessage = messaging.getSignalMessage();

            //ReceiptionAwareness or NRR found but not expected? report if configuration=true //TODO: make configurable in domibus.properties

            //SignalMessage with Receipt expected
            messageId = getMessageId(signalMessage);
            if (signalMessage.getReceipt() != null && signalMessage.getReceipt().getAny().size() == 1) {

                final String contentOfReceiptString = signalMessage.getReceipt().getAny().get(0);

                try {
                    if (!legConfiguration.getReliability().isNonRepudiation()) {
                        final UserMessage userMessage = this.jaxbContext.createUnmarshaller().unmarshal(new StreamSource(new ByteArrayInputStream(contentOfReceiptString.getBytes())), UserMessage.class).getValue();

                        final UserMessage userMessageInRequest = this.jaxbContext.createUnmarshaller().unmarshal((Node) request.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next(), Messaging.class).getValue().getUserMessage();
                        if (!userMessage.equals(userMessageInRequest)) {
                            ReliabilityChecker.LOG.warn("Reliability check failed, the user message in the request does not match the user message in the response.");
                            return CheckResult.FAIL;
                        }

                        return CheckResult.OK;
                    }

                    final Iterator<Element> elementIterator = response.getSOAPHeader().getChildElements(new QName(WSConstants.WSSE_NS, WSConstants.WSSE_LN));

                    if (!elementIterator.hasNext()) {
                        LOG.businessError(DomibusMessageCode.BUS_RELIABILITY_INVALID_WITH_NO_SECURITY_HEADER, messageId);
                        EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "Invalid NonRepudiationInformation: No security header found", null, null);
                        ex.setMshRole(MSHRole.SENDING);
                        ex.setSignalMessageId(messageId);
                        throw ex;
                    }
                    final Element securityHeaderResponse = elementIterator.next();

                    if (elementIterator.hasNext()) {
                        LOG.businessError(DomibusMessageCode.BUS_RELIABILITY_INVALID_WITH_MULTIPLE_SECURITY_HEADERS, messageId);
                        EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "Invalid NonRepudiationInformation: Multiple security headers found", null, null);
                        ex.setMshRole(MSHRole.SENDING);
                        ex.setSignalMessageId(messageId);
                        throw ex;
                    }

                    final String wsuIdOfMEssagingElement = messaging.getOtherAttributes().get(new QName(WSConstants.WSU_NS, "Id"));

                    ReliabilityChecker.LOG.debug(wsuIdOfMEssagingElement);

                    final NodeList nodeList = securityHeaderResponse.getElementsByTagNameNS(WSConstants.SIG_NS, WSConstants.REF_LN);
                    boolean signatureFound = false;
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        final Node node = nodeList.item(i);
                        if (this.compareReferenceIgnoreHashtag(node.getAttributes().getNamedItem("URI").getNodeValue(), wsuIdOfMEssagingElement)) {
                            signatureFound = true;
                            break;
                        }
                    }
                    if (!signatureFound) {
                        LOG.businessError(DomibusMessageCode.BUS_RELIABILITY_INVALID_WITH_MESSAGING_NOT_SIGNED, messageId);
                        EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "Invalid NonRepudiationInformation: eb:Messaging not signed", null, null);
                        ex.setMshRole(MSHRole.SENDING);
                        ex.setSignalMessageId(messageId);
                        throw ex;
                    }

                    final NodeList referencesFromSecurityHeader = nonRepudiationChecker.getNonRepudiationNodeList(request.getSOAPHeader().getElementsByTagNameNS(WSConstants.SIG_NS, WSConstants.SIG_LN).item(0));
                    final NodeList referencesFromNonRepudiationInformation = nonRepudiationChecker.getNonRepudiationNodeList(response.getSOAPHeader().getElementsByTagNameNS(NonRepudiationConstants.NS_NRR, NonRepudiationConstants.NRR_LN).item(0));

                    if (!nonRepudiationChecker.compareUnorderedReferenceNodeLists(referencesFromSecurityHeader, referencesFromNonRepudiationInformation)) {
                        LOG.businessError(DomibusMessageCode.BUS_RELIABILITY_INVALID_NOT_MATCHING_THE_MESSAGE, messageId);
                        EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "Invalid NonRepudiationInformation: non repudiation information and request message do not match", null, null);
                        ex.setMshRole(MSHRole.SENDING);
                        ex.setSignalMessageId(messageId);
                        throw ex;
                    }

                    LOG.businessInfo(DomibusMessageCode.BUS_RELIABILITY_SUCCESSFUL, messageId);
                    return CheckResult.OK;
                } catch (final JAXBException e) {
                    ReliabilityChecker.LOG.error("", e);
                } catch (final SOAPException e) {
                    ReliabilityChecker.LOG.error("", e);
                }

            } else {
                LOG.businessError(DomibusMessageCode.BUS_RELIABILITY_RECEIPT_INVALID_EMPTY, messageId);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "There is no content inside the receipt element received by the responding gateway", null, null);
                ex.setMshRole(MSHRole.SENDING);
                ex.setSignalMessageId(messageId);
                throw ex;
            }

        }
        LOG.businessError(DomibusMessageCode.BUS_RELIABILITY_GENERAL_ERROR, messageId);
        return CheckResult.FAIL;

    }

    protected String getMessageId(SignalMessage signalMessage) {
        if(signalMessage == null || signalMessage.getMessageInfo() == null) {
            return null;
        }
        return signalMessage.getMessageInfo().getMessageId();
    }

    /**
     * Compares two contentIds but ignores hashtags that were used for referencing inside a document
     *
     * @param referenceId the id with an hashtag
     * @param contentId   the id of the content to match
     * @return {@code true} in case both values are equal (ignoring the hashtag), else {@link false}
     */
    private boolean compareReferenceIgnoreHashtag(final String referenceId, final String contentId) {
        return referenceId.substring(1).equals(contentId);
    }

    public enum CheckResult {
        OK, FAIL, WAITING_FOR_CALLBACK
    }

}

/*
 * Copyright 2014 e-CODEX Project
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

package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;
import eu.domibus.ebms3.common.DispatchMessageCreator;
import eu.domibus.ebms3.common.MessageIdGenerator;
import eu.domibus.ebms3.common.SOAPMessageConverterService;
import eu.domibus.ebms3.common.TimestampDateFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * This service handles the generation and storage of AS4 receipts for both ReplyPattern Callback and Response
 */
@Service
public class ReceiptService {

    public static final String XSLT_GENERATE_AS4_RECEIPT_XSL = "xslt/GenerateAS4Receipt.xsl";

    private static final Log LOG = LogFactory.getLog(ReceiptService.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private MessageFactory messageFactory;

    @Autowired
    private TransformerFactory transformerFactory;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private TimestampDateFormatter timestampDateFormatter;

    @Autowired
    SOAPMessageConverterService soapMessageConverterService;

    @Resource(name = "jmsTemplateDispatchSignalMessage")
    private JmsOperations jmsOperations;

    @Autowired
    @Qualifier("sendSignalMessageQueue")
    private Queue sendSignalMessageQueue;

    /**
     * Generates AS4 receipt an incoming message
     *
     * @param request          the incoming message
     * @param legConfiguration processing information of the message
     * @return the receipt header for the incoming request message
     * @throws EbMS3Exception if generation of receipt was not successful
     */
    protected DOMSource generateReceipt(final SOAPMessage request, final LegConfiguration legConfiguration) throws EbMS3Exception {

        DOMSource domSource = null;

        try {

            InputStream generateAS4ReceiptStream = this.getClass().getClassLoader().getResourceAsStream(XSLT_GENERATE_AS4_RECEIPT_XSL);
            Source messageToReceiptTransform = new StreamSource(generateAS4ReceiptStream);
            final Transformer transformer = this.transformerFactory.newTransformer(messageToReceiptTransform);
            final Source requestMessage = request.getSOAPPart().getContent();
            transformer.setParameter("messageid", this.messageIdGenerator.generateMessageId());
            transformer.setParameter("timestamp", this.timestampDateFormatter.generateTimestamp());
            transformer.setParameter("nonRepudiation", Boolean.toString(legConfiguration.getReliability().isNonRepudiation()));

            final DOMResult domResult = new DOMResult();

            transformer.transform(requestMessage, domResult);

            domSource = new DOMSource(domResult.getNode());

        } catch (TransformerConfigurationException | SOAPException e) {
            // this cannot happen
            assert false;
            throw new RuntimeException(e);
        } catch (final TransformerException e) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0201, "Could not generate Receipt. Check security header and non-repudiation settings", null, e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }


        return domSource;
    }

    /**
     * Handles generation and processing of AS4 receipts
     *
     * @param request          the incoming message
     * @param legConfiguration processing information of the message
     * @param messageExists     indicates whether or not the message is a duplicate  @return
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public SOAPMessage handleReceipt(SOAPMessage request, LegConfiguration legConfiguration, boolean messageExists) throws EbMS3Exception {
        SOAPMessage responseMessage = null;

        SOAPMessage receiptMessage = null;

        assert legConfiguration != null;

        if (legConfiguration.getReliability() == null) {
            return responseMessage;
        }

        ReceiptService.LOG.debug("Handle reliability and reception awareness for incoming message");

        DOMSource receiptHeader = this.generateReceipt(request, legConfiguration);

        String messageId = null;
        try {
            Messaging signalMessage = null;
            receiptMessage = this.messageFactory.createMessage();
            receiptMessage.getSOAPPart().setContent(receiptHeader);

            signalMessage = soapMessageConverterService.getMessaging(receiptMessage);

            messageId = signalMessage.getSignalMessage().getMessageInfo().getMessageId();

            messagingDao.create(signalMessage);
        } catch (SOAPException | JAXBException e) {
            ReceiptService.LOG.error(e);
            throw new RuntimeException(e);
        }

        //in case replypattern = response
        if (ReplyPattern.RESPONSE.equals(legConfiguration.getReliability().getReplyPattern())) {
            responseMessage = receiptMessage;
        }

        if (ReplyPattern.CALLBACK.equals(legConfiguration.getReliability().getReplyPattern())) {
            // response should be empty in this case
            this.jmsOperations.send(sendSignalMessageQueue, new DispatchMessageCreator(messageId, null));
        }


        return responseMessage;
    }

    protected String convertDOMtoString(DOMSource receiptAsDOM) {

        try {
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            Transformer transformer = this.transformerFactory.newTransformer();
            transformer.transform(receiptAsDOM, streamResult);
            stringWriter.flush();
            return stringWriter.toString();
        } catch (TransformerException e) {
            ReceiptService.LOG.error("Could not convert receipt to string", e);
            return null;
        }

    }

}

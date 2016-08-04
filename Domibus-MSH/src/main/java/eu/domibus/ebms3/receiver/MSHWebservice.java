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

package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationType;
import eu.domibus.common.dao.MessageLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.common.model.logging.MessageLogEntry;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.plugin.SubmissionValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.attachment.AttachmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipException;

/**
 * This method is responsible for the receiving of ebMS3 messages and the sending of signal messages like receipts or ebMS3 errors in return
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@WebServiceProvider(portName = "mshPort", serviceName = "mshService")
@ServiceMode(Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MSHWebservice implements Provider<SOAPMessage> {

    public static final String XSLT_GENERATE_AS4_RECEIPT_XSL = "xslt/GenerateAS4Receipt.xsl";
    private static final Log LOG = LogFactory.getLog(MSHWebservice.class);
    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private MessageFactory messageFactory;

    @Autowired
    private MessageLogDao messageLogDao;

    private JAXBContext jaxbContext;

    @Autowired
    private TransformerFactory transformerFactory;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private TimestampDateFormatter timestampDateFormatter;

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private PayloadProfileValidator payloadProfileValidator;

    @Autowired
    private PropertyProfileValidator propertyProfileValidator;

    public void setJaxbContext(final JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    @Override
    @Transactional
    public SOAPMessage invoke(final SOAPMessage request) {

        final SOAPMessage responseMessage;

        String pmodeKey = null;
        try {
            //FIXME: use a consistent way of property exchange between JAXWS and CXF message model. This: PropertyExchangeInterceptor
            pmodeKey = (String) request.getProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
        } catch (final SOAPException soapEx) {
            //this error should never occur because pmode handling is done inside the in-interceptorchain
            LOG.error("Cannot find PModeKey property for incoming Message", soapEx);
            assert false;
        }

        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pmodeKey);
        Messaging messaging = null;
        boolean pingMessage = false;
        try (StringWriter sw = new StringWriter()) {
            if (MSHWebservice.LOG.isDebugEnabled()) {

                this.transformerFactory.newTransformer().transform(
                        new DOMSource(request.getSOAPPart()),
                        new StreamResult(sw));

                MSHWebservice.LOG.debug(sw.toString());
                MSHWebservice.LOG.debug("received attachments:");
                final Iterator i = request.getAttachments();
                while (i.hasNext()) {
                    MSHWebservice.LOG.debug(i.next());
                }
            }
            messaging = this.getMessaging(request);

            checkCharset(messaging);
            pingMessage = checkPingMessage(messaging.getUserMessage());
            final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(messaging);

            if (!messageExists && !pingMessage) { // ping messages are not stored/delivered
                this.persistReceivedMessage(request, legConfiguration, pmodeKey, messaging);
                try {
                    backendNotificationService.notifyOfIncoming(messaging.getUserMessage(), NotificationType.MESSAGE_RECEIVED);
                } catch(SubmissionValidationException e) {
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, e.getMessage(), null, e);
                }
            }
            responseMessage = this.generateReceipt(request, legConfiguration, messageExists);

        } catch (TransformerException | SOAPException | JAXBException | IOException e) {
            throw new RuntimeException(e);
        } catch (final EbMS3Exception e) {
            try {
                if (!pingMessage && legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer() && messaging != null) {
                    backendNotificationService.notifyOfIncoming(messaging.getUserMessage(), NotificationType.MESSAGE_RECEIVED_FAILURE);
                }
            } catch (Exception ex) {
                LOG.warn("could not notify backend of rejected message ", ex);
            }
            throw new WebServiceException(e);
        }

        return responseMessage;
    }


    /**
     * Required for AS4_TA_12
     *
     * @param messaging
     * @throws EbMS3Exception
     */
    private void checkCharset(final Messaging messaging) throws EbMS3Exception {
        for (final PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            for (final Property property : partInfo.getPartProperties().getProperties()) {
                if (Property.CHARSET.equals(property.getName()) && !Property.CHARSET_PATTERN.matcher(property.getValue()).matches()) {
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, property.getValue() + " is not a valid Charset", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
                    ex.setMshRole(MSHRole.RECEIVING);
                    throw ex;
                }
            }
        }
    }

    /**
     * If message with same messageId is already in the database return <code>true</code> else <code>false</code>
     *
     * @param messaging
     * @return result of duplicate check
     */
    private Boolean checkDuplicate(final Messaging messaging) {
        return messageLogDao.findByMessageId(messaging.getUserMessage().getMessageInfo().getMessageId(), MSHRole.RECEIVING) != null;
    }


    /**
     * Check if this message is a ping message
     *
     * @param message
     * @return result of ping service and action check
     */
    private Boolean checkPingMessage(final UserMessage message) {

        return eu.domibus.common.model.configuration.Service.TEST_SERVICE.equals(message.getCollaborationInfo().getService().getValue())
                && eu.domibus.common.model.configuration.Action.TEST_ACTION.equals(message.getCollaborationInfo().getAction());

    }

    /**
     * Handles Receipt generation for a incoming message
     *
     * @param request          the incoming message
     * @param legConfiguration processing information of the message
     * @param duplicate        indicates whether or not the message is a duplicate
     * @return the response message to the incoming request message
     * @throws EbMS3Exception if generation of receipt was not successful
     */
    private SOAPMessage generateReceipt(final SOAPMessage request, final LegConfiguration legConfiguration, final Boolean duplicate) throws EbMS3Exception {
        SOAPMessage responseMessage = null;

        assert legConfiguration != null;

        if (legConfiguration.getReliability() == null) {
            return responseMessage;
        }

        if (ReplyPattern.RESPONSE.equals(legConfiguration.getReliability().getReplyPattern())) {
            MSHWebservice.LOG.debug("Checking reliability for incoming message");
            try {
                responseMessage = this.messageFactory.createMessage();
                InputStream generateAS4ReceiptStream = this.getClass().getClassLoader().getResourceAsStream(XSLT_GENERATE_AS4_RECEIPT_XSL);
                Source messageToReceiptTransform = new StreamSource(generateAS4ReceiptStream);
                final Transformer transformer = this.transformerFactory.newTransformer(messageToReceiptTransform);
                final Source requestMessage = request.getSOAPPart().getContent();
                transformer.setParameter("messageid", this.messageIdGenerator.generateMessageId());
                transformer.setParameter("timestamp", this.timestampDateFormatter.generateTimestamp());
                transformer.setParameter("nonRepudiation", Boolean.toString(legConfiguration.getReliability().isNonRepudiation()));

                final DOMResult domResult = new DOMResult();

                transformer.transform(requestMessage, domResult);
                responseMessage.getSOAPPart().setContent(new DOMSource(domResult.getNode()));

//                transformer.transform(requestMessage, new DOMResult(responseMessage.getSOAPPart().getEnvelope()));
            } catch (TransformerConfigurationException | SOAPException e) {
                // this cannot happen
                assert false;
                throw new RuntimeException(e);
            } catch (final TransformerException e) {
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0201, "Could not generate Receipt. Check security header and non-repudiation settings", null, e);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
        }

        return responseMessage;
    }

    /**
     * This method persists incoming messages into the database (and handles decompression before)
     *
     * @param request          the message to persist
     * @param legConfiguration processing information for the message
     * @throws SOAPException
     * @throws JAXBException
     * @throws TransformerException
     * @throws IOException
     * @throws EbMS3Exception
     */
    //TODO: improve error handling
    private String persistReceivedMessage(final SOAPMessage request, final LegConfiguration legConfiguration, final String pmodeKey, final Messaging messaging) throws SOAPException, JAXBException, TransformerException, EbMS3Exception {


        boolean bodyloadFound = false;
        for (final PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            final String cid = partInfo.getHref();
            MSHWebservice.LOG.debug("looking for attachment with cid: " + cid);
            boolean payloadFound = false;
            if (cid == null || cid.isEmpty() || cid.startsWith("#")) {
                if (bodyloadFound) {
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "More than one Partinfo referencing the soap body found", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
                    ex.setMshRole(MSHRole.RECEIVING);
                    throw ex;
                }
                bodyloadFound = true;
                payloadFound = true;
                partInfo.setInBody(true);
                final Node bodyContent = (((Node) request.getSOAPBody().getChildElements().next()));
                final Source source = new DOMSource(bodyContent);
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final Result result = new StreamResult(out);
                final Transformer transformer = this.transformerFactory.newTransformer();
                transformer.transform(source, result);
                partInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(out.toByteArray(), "text/xml")));
            }
            @SuppressWarnings("unchecked") final
            Iterator<AttachmentPart> attachmentIterator = request.getAttachments();
            AttachmentPart attachmentPart;
            while (attachmentIterator.hasNext() && !payloadFound) {

                attachmentPart = attachmentIterator.next();
                //remove square brackets from cid for further processing
                attachmentPart.setContentId(AttachmentUtil.cleanContentId(attachmentPart.getContentId()));
                MSHWebservice.LOG.debug("comparing with: " + attachmentPart.getContentId());
                if (attachmentPart.getContentId().equals(AttachmentUtil.cleanContentId(cid))) {
                    partInfo.setPayloadDatahandler(attachmentPart.getDataHandler());
                    partInfo.setInBody(false);
                    payloadFound = true;
                }
            }
            if (!payloadFound) {
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0011, "No Attachment found for cid: " + cid + " of message: " + messaging.getUserMessage().getMessageInfo().getMessageId(), messaging.getUserMessage().getMessageInfo().getMessageId(), null);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
        }

        final boolean compressed = this.compressionService.handleDecompression(messaging.getUserMessage(), legConfiguration);
        try {
            this.payloadProfileValidator.validate(messaging, pmodeKey);
            this.propertyProfileValidator.validate(messaging, pmodeKey);
        } catch (EbMS3Exception e) {
            e.setMshRole(MSHRole.RECEIVING);
            throw e;
        }
        MSHWebservice.LOG.debug("Compression for message with id: " + messaging.getUserMessage().getMessageInfo().getMessageId() + " applied: " + compressed);
        final MessageLogEntry messageLogEntry = new MessageLogEntry();
        messageLogEntry.setMessageId(messaging.getUserMessage().getMessageInfo().getMessageId());
        messageLogEntry.setMessageType(MessageType.USER_MESSAGE);
        messageLogEntry.setMshRole(MSHRole.RECEIVING);
        messageLogEntry.setReceived(new Date());
        final String mpc = messaging.getUserMessage().getMpc();
        messageLogEntry.setMpc((mpc == null || mpc.isEmpty()) ? Mpc.DEFAULT_MPC : mpc);
        messageLogEntry.setMessageStatus(MessageStatus.RECEIVED);
        this.messageLogDao.create(messageLogEntry);
        try {
            this.messagingDao.create(messaging);
        } catch (Exception exc) {
            LOG.error("Could not persist message " + exc.getMessage());
            if(exc instanceof ZipException ||
                    (exc.getCause() != null && exc.getCause() instanceof ZipException)) {
                LOG.debug("InstanceOf ZipException");
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "Could not persist message" + exc.getMessage(), messaging.getUserMessage().getMessageInfo().getMessageId(), exc);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;

            }
            throw exc;
        }

        return messageLogEntry.getMessageId();
    }

    private Messaging getMessaging(final SOAPMessage request) throws SOAPException, JAXBException {
        final Node messagingXml = (Node) request.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        @SuppressWarnings("unchecked") final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }
}

package eu.domibus.ebms3.receiver;

import eu.domibus.common.*;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.common.model.logging.SignalMessageLogBuilder;
import eu.domibus.common.model.logging.UserMessageLogBuilder;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.util.SoapUtil;
import org.apache.commons.lang.StringUtils;
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
import javax.xml.soap.*;
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
    private SignalMessageDao signalMessageDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private MessageFactory messageFactory;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

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

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

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
            if (LOG.isDebugEnabled()) {

                this.transformerFactory.newTransformer().transform(
                        new DOMSource(request.getSOAPPart()),
                        new StreamResult(sw));

                LOG.debug(sw.toString());
                LOG.debug("received attachments:");
                final Iterator i = request.getAttachments();
                while (i.hasNext()) {
                    LOG.debug(i.next());
                }
            }
            messaging = this.getMessaging(request);

            checkCharset(messaging);
            pingMessage = checkPingMessage(messaging.getUserMessage());
            final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(messaging);

            if (!messageExists && !pingMessage) { // ping messages are not stored/delivered
                this.persistReceivedMessage(request, legConfiguration, pmodeKey, messaging);
                try {
                    backendNotificationService.notifyMessageReceived(messaging.getUserMessage());
                } catch (SubmissionValidationException e) {
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, e.getMessage(), null, e);
                }
            }
            responseMessage = this.generateReceipt(request, legConfiguration, messageExists);

        } catch (TransformerException | SOAPException | JAXBException | IOException e) {
            throw new RuntimeException(e);
        } catch (final EbMS3Exception e) {
            try {
                if (!pingMessage && legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer() && messaging != null) {
                    backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), createErrorResult(e));
                }
            } catch (Exception ex) {
                LOG.warn("could not notify backend of rejected message ", ex);
            }
            throw new WebServiceException(e);
        }

        return responseMessage;
    }

    private ErrorResult createErrorResult(EbMS3Exception ebm3Exception) {
        ErrorResultImpl result = new ErrorResultImpl();
        result.setMshRole(MSHRole.RECEIVING);
        result.setMessageInErrorId(ebm3Exception.getRefToMessageId());
        try {
            result.setErrorCode(ebm3Exception.getErrorCodeObject());
        } catch (IllegalArgumentException e) {
            LOG.warn("Could not find error code for [" + ebm3Exception.getErrorCode() + "]");
        }
        result.setErrorDetail(ebm3Exception.getErrorDetail());
        return result;
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
     * @return result of duplicate handle
     */
    private Boolean checkDuplicate(final Messaging messaging) {
        return userMessageLogDao.findByMessageId(messaging.getUserMessage().getMessageInfo().getMessageId(), MSHRole.RECEIVING) != null;
    }


    /**
     * Check if this message is a ping message
     *
     * @param message
     * @return result of ping service and action handle
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
            LOG.debug("Checking reliability for incoming message");
            try {
                responseMessage = messageFactory.createMessage();
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
                saveResponse(responseMessage);

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

    private void saveResponse(final SOAPMessage responseMessage) {
        try {
            Messaging messaging = getMessaging(responseMessage);
            final SignalMessage signalMessage = messaging.getSignalMessage();
            // Stores the signal message
            signalMessageDao.create(signalMessage);
            // Updating the reference to the signal message
            Messaging sentMessage = messagingDao.findMessageByMessageId(messaging.getSignalMessage().getMessageInfo().getRefToMessageId());
            if (sentMessage != null) {
                sentMessage.setSignalMessage(signalMessage);
                messagingDao.update(sentMessage);
            }
            // Builds the signal message log
            SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                    .setMessageId(messaging.getSignalMessage().getMessageInfo().getMessageId())
                    .setMessageStatus(MessageStatus.SEND_IN_PROGRESS)
                    .setMshRole(MSHRole.SENDING)
                    .setNotificationStatus(NotificationStatus.NOT_REQUIRED);
            // Saves an entry of the signal message log
            signalMessageLogDao.create(smlBuilder.build());
        } catch (JAXBException | SOAPException ex) {
            LOG.error("Unable to save the SignalMessage due to error: ", ex);
        }

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

        UserMessage userMessage = messaging.getUserMessage();

        handlePayloads(request, userMessage);

        boolean compressed = compressionService.handleDecompression(userMessage, legConfiguration);
        try {
            this.payloadProfileValidator.validate(messaging, pmodeKey);
            this.propertyProfileValidator.validate(messaging, pmodeKey);
        } catch (EbMS3Exception e) {
            e.setMshRole(MSHRole.RECEIVING);
            throw e;
        }
        LOG.debug("Compression for message with id: " + userMessage.getMessageInfo().getMessageId() + " applied: " + compressed);

        try {
            messagingDao.create(messaging);

            Party to = pModeProvider.getReceiverParty(pmodeKey);

            // Builds the user message log
            UserMessageLogBuilder umlBuilder = UserMessageLogBuilder.create()
                    .setMessageId(userMessage.getMessageInfo().getMessageId())
                    .setMessageStatus(MessageStatus.RECEIVED)
                    .setMshRole(MSHRole.RECEIVING)
                    .setNotificationStatus(legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer() ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED)
                    .setMpc(StringUtils.isEmpty(userMessage.getMpc()) ? Mpc.DEFAULT_MPC : userMessage.getMpc())
                    .setSendAttemptsMax(0)
                    .setBackendName(getFinalRecipientName(userMessage))
                    .setEndpoint(to.getEndpoint());
            // Saves the user message log
            userMessageLogDao.create(umlBuilder.build());

        } catch (Exception exc) {
            LOG.error("Could not persist message " + exc.getMessage());
            if (exc instanceof ZipException || (exc.getCause() != null && exc.getCause() instanceof ZipException)) {
                LOG.debug("InstanceOf ZipException");
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "Could not persist message" + exc.getMessage(), userMessage.getMessageInfo().getMessageId(), exc);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
            throw exc;
        }

        try {
            String rawXMLMessage = SoapUtil.getRawXMLMessage(request);
            LOG.debug("Persist raw XML envelope: " + rawXMLMessage);
            RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
            rawEnvelopeLog.setRawXML(rawXMLMessage);
            rawEnvelopeLog.setUserMessage(userMessage);
            rawEnvelopeLogDao.create(rawEnvelopeLog);
        } catch (TransformerException e) {
            LOG.warn("Unable to log the raw message XML due to: ", e);
        }

        return userMessage.getMessageInfo().getMessageId();
    }

    private String getFinalRecipientName(UserMessage userMessage) {
        for (Property property : userMessage.getMessageProperties().getProperty()) {
            if (property.getName() != null && property.getName().equals(MessageConstants.FINAL_RECIPIENT)) {
                return property.getValue();
            }
        }
        return null;
    }

    private void handlePayloads(SOAPMessage request, UserMessage userMessage) throws EbMS3Exception, SOAPException, TransformerException {
        boolean bodyloadFound = false;
        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            final String cid = partInfo.getHref();
            LOG.debug("looking for attachment with cid: " + cid);
            boolean payloadFound = false;
            if (cid == null || cid.isEmpty() || cid.startsWith("#")) {
                if (bodyloadFound) {
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "More than one Partinfo referencing the soap body found", userMessage.getMessageInfo().getMessageId(), null);
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
                LOG.debug("comparing with: " + attachmentPart.getContentId());
                if (attachmentPart.getContentId().equals(AttachmentUtil.cleanContentId(cid))) {
                    partInfo.setPayloadDatahandler(attachmentPart.getDataHandler());
                    partInfo.setInBody(false);
                    payloadFound = true;
                }
            }
            if (!payloadFound) {
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0011, "No Attachment found for cid: " + cid + " of message: " + userMessage.getMessageInfo().getMessageId(), userMessage.getMessageInfo().getMessageId(), null);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
        }
    }

    private Messaging getMessaging(final SOAPMessage soapMessage) throws SOAPException, JAXBException {
        final Node messagingXml = (Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        @SuppressWarnings("unchecked") final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }
}

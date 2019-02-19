package eu.domibus.plugin.jms;

import com.google.common.io.CharStreams;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.ext.services.NonRepudiationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.common.WSS4JConstants;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class BackendJMSImpl extends AbstractBackendConnector<MapMessage, MapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSImpl.class);

    protected static final String JMSPLUGIN_QUEUE_REPLY = "jmsplugin.queue.reply";
    protected static final String JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR = "jmsplugin.queue.consumer.notification.error";
    protected static final String JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR = "jmsplugin.queue.producer.notification.error";
    protected static final String JMSPLUGIN_QUEUE_OUT = "jmsplugin.queue.out";

    @Autowired
    protected JMSExtService jmsExtService;

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    protected DomainContextExtService domainContextExtService;

    @Autowired
    @Qualifier(value = "mshToBackendTemplate")
    private JmsOperations mshToBackendTemplate;

    @Autowired
    protected NonRepudiationExtService nonRepudiationExtService;

    private MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer;
    private MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer;

    public BackendJMSImpl(String name) {
        super(name);
    }

    @Override
    public MessageSubmissionTransformer<MapMessage> getMessageSubmissionTransformer() {
        return this.messageSubmissionTransformer;
    }

    public void setMessageSubmissionTransformer(MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer) {
        this.messageSubmissionTransformer = messageSubmissionTransformer;
    }

    @Override
    public MessageRetrievalTransformer<MapMessage> getMessageRetrievalTransformer() {
        return this.messageRetrievalTransformer;
    }

    public void setMessageRetrievalTransformer(MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer) {
        this.messageRetrievalTransformer = messageRetrievalTransformer;
    }

    /**
     * This method is called when a message was received at the incoming queue
     *
     * @param map The incoming JMS Message
     */
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Transactional
    public void receiveMessage(final MapMessage map) {
        try {
            String messageID = map.getStringProperty(MESSAGE_ID);
            if (StringUtils.isNotBlank(messageID)) {
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageID);
            }
            final String jmsCorrelationID = map.getJMSCorrelationID();
            final String messageType = map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);

            LOG.info("Received message with messageId [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]");

            if (!MESSAGE_TYPE_SUBMIT.equals(messageType)) {
                String wrongMessageTypeMessage = getWrongMessageTypeErrorMessage(messageID, jmsCorrelationID, messageType);
                LOG.error(wrongMessageTypeMessage);
                sendReplyMessage(messageID, wrongMessageTypeMessage, jmsCorrelationID);
                return;
            }

            String errorMessage = null;
            try {
                //in case the messageID is not sent by the user it will be generated
                messageID = submit(map);
            } catch (final MessagingProcessingException e) {
                LOG.error("Exception occurred receiving message [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]", e);
                errorMessage = e.getMessage() + ": Error Code: " + (e.getEbms3ErrorCode() != null ? e.getEbms3ErrorCode().getErrorCodeName() : " not set");
            }

            sendReplyMessage(messageID, errorMessage, jmsCorrelationID);

            LOG.info("Submitted message with messageId [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]");
        } catch (Exception e) {
            LOG.error("Exception occurred while receiving message [" + map + "]", e);
            throw new DefaultJmsPluginException("Exception occurred while receiving message [" + map + "]", e);
        }
    }

    protected String getWrongMessageTypeErrorMessage(String messageID, String jmsCorrelationID, String messageType) {
        return MessageFormat.format("Illegal messageType [{0}] on message with JMSCorrelationId [{1}] and messageId [{2}]. Only [{3}] messages are accepted on this queue",
                messageType, jmsCorrelationID, messageID, MESSAGE_TYPE_SUBMIT);
    }

    protected void sendReplyMessage(final String messageId, final String errorMessage, final String correlationId) {
        LOG.debug("Sending reply message");
        final JmsMessageDTO jmsMessageDTO = new ReplyMessageCreator(messageId, errorMessage, correlationId).createMessage();
        sendJmsMessage(jmsMessageDTO, JMSPLUGIN_QUEUE_REPLY);
    }

    @Override
    public void deliverMessage(final String messageId) {
        LOG.debug("Delivering message");
        final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
        final String queueValue = domibusPropertyExtService.getDomainProperty(currentDomain, JMSPLUGIN_QUEUE_OUT);
        if (StringUtils.isEmpty(queueValue)) {
            throw new DomibusPropertyExtException("Error getting the queue [" + JMSPLUGIN_QUEUE_OUT + "]");
        }
        LOG.info("Sending message to queue [{}]", queueValue);
        mshToBackendTemplate.send(queueValue, new DownloadMessageCreator(messageId));
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        LOG.debug("Handling messageReceiveFailed");
        final JmsMessageDTO jmsMessageDTO = new ErrorMessageCreator(messageReceiveFailureEvent.getErrorResult(),
                messageReceiveFailureEvent.getEndpoint(),
                NotificationType.MESSAGE_RECEIVED_FAILURE).createMessage();
        sendJmsMessage(jmsMessageDTO, JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR);
    }

    @Override
    public void messageSendFailed(final String messageId) {
        List<ErrorResult> errors = super.getErrorsForMessage(messageId);
        final JmsMessageDTO jmsMessageDTO = new ErrorMessageCreator(errors.get(errors.size() - 1), null, NotificationType.MESSAGE_SEND_FAILURE).createMessage();
        sendJmsMessage(jmsMessageDTO, JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR);
    }

    @Override
    public void messageSendSuccess(String messageId) {
        LOG.debug("Handling messageSendSuccess");
        final JmsMessageDTO jmsMessageDTO = new SignalMessageCreator(messageId, NotificationType.MESSAGE_SEND_SUCCESS).createMessage();
        sendJmsMessage(jmsMessageDTO, JMSPLUGIN_QUEUE_REPLY);
    }

    protected void sendJmsMessage(JmsMessageDTO message, String queueProperty) {
        final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
        final String queueValue = domibusPropertyExtService.getDomainProperty(currentDomain, queueProperty);
        if (StringUtils.isEmpty(queueValue)) {
            throw new DomibusPropertyExtException("Error getting the queue [" + queueProperty + "]");
        }
        LOG.info("Sending message to queue [{}]", queueValue);
        jmsExtService.sendMapMessageToQueue(message, queueValue);
    }

    @Override
    public MapMessage downloadMessage(String messageId, MapMessage target) throws MessageNotFoundException {
        LOG.debug("Downloading message");
        return this.getMessageRetrievalTransformer().transformFromSubmission(this.messageRetriever.downloadMessage(messageId), target);
    }

    private class DownloadMessageCreator implements MessageCreator {
        private String messageId;


        public DownloadMessageCreator(final String messageId) {
            this.messageId = messageId;
        }

        @Override
        public Message createMessage(final Session session) throws JMSException {
            final MapMessage mapMessage = session.createMapMessage();
            try {
                downloadMessage(messageId, mapMessage);
                final String rawXmlByMessageId = nonRepudiationExtService.getRawXmlByMessageId(messageId);
                final List<String> samlAssertions = getSamlAssertions(rawXmlByMessageId);
                for (int i = 0; i < samlAssertions.size(); i++) {
                    String assertion = samlAssertions.get(i);
                    mapMessage.setStringProperty("saml" + i, assertion);
                }
            } catch (final Exception e) {
                throw new DefaultJmsPluginException("Unable to create push message", e);
            }
            mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_INCOMING);
            final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
            mapMessage.setStringProperty(MessageConstants.DOMAIN, currentDomain.getCode());
            return mapMessage;
        }
    }

    protected List<String> getSamlAssertions(final String rawXml) throws SOAPException, IOException, ParserConfigurationException, SAXException, TransformerException {
        List<String> result = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();

        try (StringReader stringReader = new StringReader(rawXml); InputStream targetStream =
                new ByteArrayInputStream(CharStreams.toString(stringReader)
                        .getBytes(Charsets.UTF_8.name()))) {
            Document document = builder.parse(targetStream);
            final NodeList assertionsNodeList = document.getDocumentElement().getElementsByTagNameNS(WSConstants.SAML2_NS, WSConstants.ASSERTION_LN);
            final int length = assertionsNodeList.getLength();
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    final Element assertionNode = (Element) assertionsNodeList.item(i);
                    if (assertionNode.hasAttributeNS(WSS4JConstants.WSU_NS, "Id")) {
                        assertionNode.removeAttributeNS(WSS4JConstants.WSU_NS, "Id");
                    }
                    final String rawXMLMessage = getRawXMLMessage(assertionNode);
                    LOG.info("Added SAML assertion [[]]", rawXMLMessage);
                    result.add(rawXMLMessage);
                }
            }
        }

        return result;
    }

    protected String getRawXMLMessage(Node assertion) throws TransformerException {
        final StringWriter sw = new StringWriter();

        TransformerFactory.newInstance().newTransformer().transform(
                new DOMSource(assertion),
                new StreamResult(sw));

        return  sw.toString();
    }
}

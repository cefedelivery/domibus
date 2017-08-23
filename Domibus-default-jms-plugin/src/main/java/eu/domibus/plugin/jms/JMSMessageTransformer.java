
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.domibus.plugin.jms;


import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.io.IOUtils;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * This class is responsible for transformations from {@link javax.jms.MapMessage} to {@link eu.domibus.plugin.Submission} and vice versa
 *
 * @author Padraig McGourty, Christian Koch, Stefan Mueller
 */


public class JMSMessageTransformer
        implements MessageRetrievalTransformer<MapMessage>, MessageSubmissionTransformer<MapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSMessageTransformer.class);

    /**
     * The default properties to be used
     */

    private Properties properties;

    public JMSMessageTransformer(){
        properties=new Properties();
    }

    public JMSMessageTransformer(String defaultProperties) throws IOException {
        properties=new Properties();
        properties.load(new FileReader(defaultProperties));
    }

    /**
     * Transforms {@link eu.domibus.plugin.Submission} to {@link javax.jms.MapMessage}
     *
     * @param submission the message to be transformed     *
     * @return result of the transformation as {@link javax.jms.MapMessage}
     */
    @Override
    public MapMessage transformFromSubmission(final Submission submission, final MapMessage messageOut) {
        try {
            messageOut.setStringProperty(ACTION, submission.getAction());
            messageOut.setStringProperty(SERVICE, submission.getService());
            messageOut.setStringProperty(SERVICE_TYPE, submission.getServiceType());
            messageOut.setStringProperty(CONVERSATION_ID, submission.getConversationId());
            messageOut.setStringProperty(MESSAGE_ID, submission.getMessageId());

            for (final Submission.Party fromParty : submission.getFromParties()) {
                messageOut.setStringProperty(FROM_PARTY_ID, fromParty.getPartyId());
                messageOut.setStringProperty(FROM_PARTY_TYPE, fromParty.getPartyIdType());
            }
            messageOut.setStringProperty(FROM_ROLE, submission.getFromRole());

            for (final Submission.Party toParty : submission.getToParties()) {
                messageOut.setStringProperty(TO_PARTY_ID, toParty.getPartyId());
                messageOut.setStringProperty(TO_PARTY_TYPE, toParty.getPartyIdType());
            }
            messageOut.setStringProperty(TO_ROLE, submission.getToRole());


            for (final Submission.TypedProperty p : submission.getMessageProperties()) {
                if (p.getKey().equals(PROPERTY_ORIGINAL_SENDER)) {
                    messageOut.setStringProperty(PROPERTY_ORIGINAL_SENDER, p.getValue());
                    continue;
                }
                if (p.getKey().equals(PROPERTY_ENDPOINT)) {
                    messageOut.setStringProperty(PROPERTY_ENDPOINT, p.getValue());
                    continue;
                }
                if (p.getKey().equals(PROPERTY_FINAL_RECIPIENT)) {
                    messageOut.setStringProperty(PROPERTY_FINAL_RECIPIENT, p.getValue());
                    continue;
                }
                //only reached if none of the predefined properties are set
                messageOut.setStringProperty(PROPERTY_PREFIX + p.getKey(), p.getValue());
                messageOut.setStringProperty(PROPERTY_TYPE_PREFIX + p.getKey(), p.getType());
            }
            messageOut.setStringProperty(PROTOCOL, "AS4");
            messageOut.setStringProperty(AGREEMENT_REF, submission.getAgreementRef());
            messageOut.setStringProperty(REF_TO_MESSAGE_ID, submission.getRefToMessageId());

            int counter = 1;
            for (final Submission.Payload p : submission.getPayloads()) {
                if (p.isInBody()) {
                    counter = 2;
                    break;
                }
            }

            final boolean putAttachmentsInQueue = Boolean.parseBoolean(properties.getProperty(PUT_ATTACHMENTS_IN_QUEUE, "true"));
            for (final Submission.Payload p : submission.getPayloads()) {
                transformFromSubmissionHandlePayload(messageOut, putAttachmentsInQueue, counter, p);
                counter++;
            }
            messageOut.setIntProperty(TOTAL_NUMBER_OF_PAYLOADS, submission.getPayloads().size());
        } catch (final JMSException | IOException ex) {
            LOG.error("Error while filling the MapMessage", ex);
            throw new DefaultJmsPluginException(ex);
        }

        return messageOut;
    }

    private void transformFromSubmissionHandlePayload(MapMessage messageOut, boolean putAttachmentsInQueue, int counter, Submission.Payload p) throws JMSException, IOException {
        if (p.isInBody()) {
            if(p.getPayloadDatahandler() != null) {
                messageOut.setBytes(MessageFormat.format(PAYLOAD_NAME_FORMAT, 1), IOUtils.toByteArray(p.getPayloadDatahandler().getInputStream()));
            }

            messageOut.setStringProperty(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, 1), findMime(p.getPayloadProperties()));
            messageOut.setStringProperty(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, 1), p.getContentId());
            if (p.getDescription() != null) {
                messageOut.setStringProperty(MessageFormat.format(PAYLOAD_DESCRIPTION_FORMAT, 1), p.getDescription().getValue());
            }
        } else {
            final String payContID = String.valueOf(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, counter));
            final String payDescrip = String.valueOf(MessageFormat.format(PAYLOAD_DESCRIPTION_FORMAT, counter));
            final String propPayload = String.valueOf(MessageFormat.format(PAYLOAD_NAME_FORMAT, counter));
            final String payMimeTypeProp = String.valueOf(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, counter));
            final String payFileNameProp = String.valueOf(MessageFormat.format(PAYLOAD_FILE_NAME_FORMAT, counter));
            if(p.getPayloadDatahandler() != null ) {
                if (putAttachmentsInQueue) {
                    LOG.debug("putAttachmentsInQueue is true");
                    messageOut.setBytes(propPayload, IOUtils.toByteArray(p.getPayloadDatahandler().getInputStream()));
                } else {
                    LOG.debug("putAttachmentsInQueue is false");
                    messageOut.setStringProperty(payFileNameProp, findFilename(p.getPayloadProperties()));
                }
            }
            messageOut.setStringProperty(payMimeTypeProp, findMime(p.getPayloadProperties()));
            messageOut.setStringProperty(payContID, p.getContentId());

            if (p.getDescription() != null) {
                messageOut.setStringProperty(payDescrip, p.getDescription().getValue());
            }
        }
    }

    private String findElement(String element, Collection<Submission.TypedProperty> props) {
        for (Submission.TypedProperty prop : props) {
            if (element.equals(prop.getKey()) && isEmpty(trim(prop.getType()))) {
                return prop.getValue();
            }
        }
        return null;
    }

    private String findMime(Collection<Submission.TypedProperty> props) {
        return findElement(MIME_TYPE, props);
    }

    private String findFilename(Collection<Submission.TypedProperty> props) {
        return findElement(PAYLOAD_FILENAME, props);
    }

    /**
     * Transforms {@link javax.jms.MapMessage} to {@link eu.domibus.plugin.Submission}
     *
     * @param messageIn the message ({@link javax.jms.MapMessage}) to be tranformed
     * @return the result of the transformation as {@link eu.domibus.plugin.Submission}
     */
    @Override
    public Submission transformToSubmission(final MapMessage messageIn) {
        final Submission target = new Submission();
        try {
            target.setMessageId(trim(messageIn.getStringProperty(MESSAGE_ID)));

            setTargetFromPartyIdAndFromPartyType(messageIn, target);

            target.setFromRole(trim(messageIn.getStringProperty(FROM_ROLE)));
            if (isEmpty(target.getFromRole())) {
                target.setFromRole(properties.getProperty(FROM_ROLE));
            }

            setTargetToPartyIdAndToPartyType(messageIn, target);

            target.setToRole(trim(messageIn.getStringProperty(TO_ROLE)));
            if (isEmpty(target.getToRole())) {
                target.setToRole(properties.getProperty(TO_ROLE));
            }

            target.setAction(trim(messageIn.getStringProperty(ACTION)));
            if (isEmpty(target.getAction())) {
                target.setAction(properties.getProperty(ACTION));
            }

            target.setService(trim(messageIn.getStringProperty(SERVICE)));
            if (isEmpty(target.getService())) {
                target.setService(properties.getProperty(SERVICE));
            }

            target.setServiceType(trim(messageIn.getStringProperty(SERVICE_TYPE)));
            if (isEmpty(target.getServiceType())) {
                target.setServiceType(properties.getProperty(SERVICE_TYPE));
            }

            target.setAgreementRef(trim(messageIn.getStringProperty(AGREEMENT_REF)));
            if (isEmpty(target.getAgreementRef())) {
                target.setAgreementRef(properties.getProperty(AGREEMENT_REF));
            }

            target.setConversationId(trim(messageIn.getStringProperty(CONVERSATION_ID)));

            //not part of ebMS3, eCODEX legacy property
            String strOriginalSender = trim(messageIn.getStringProperty(PROPERTY_ORIGINAL_SENDER));
            if (!isEmpty(strOriginalSender)) {
                target.addMessageProperty(PROPERTY_ORIGINAL_SENDER, strOriginalSender);
            }

            String endpoint = trim(messageIn.getStringProperty(PROPERTY_ENDPOINT));
            if (!isEmpty(endpoint)) {
                target.addMessageProperty(PROPERTY_ENDPOINT, messageIn.getStringProperty(PROPERTY_ENDPOINT));
            }

            //not part of ebMS3, eCODEX legacy property
            String strFinalRecipient = trim(messageIn.getStringProperty(PROPERTY_FINAL_RECIPIENT));
            if (!isEmpty(strFinalRecipient)) {
                target.addMessageProperty(PROPERTY_FINAL_RECIPIENT, strFinalRecipient);
            }

            target.setRefToMessageId(trim(messageIn.getStringProperty(REF_TO_MESSAGE_ID)));

            final int numPayloads = messageIn.getIntProperty(TOTAL_NUMBER_OF_PAYLOADS);
            String bodyloadEnabled = trim(messageIn.getStringProperty(JMSMessageConstants.P1_IN_BODY));
            if (isEmpty(bodyloadEnabled)) {
                bodyloadEnabled = properties.getProperty(P1_IN_BODY);
            }

            Enumeration<String> allProps = messageIn.getPropertyNames();
            while (allProps.hasMoreElements()) {
                String key = allProps.nextElement();
                if (key.startsWith(PROPERTY_PREFIX)) {
                    target.addMessageProperty(key.substring(PROPERTY_PREFIX.length()), trim(messageIn.getStringProperty(key)), trim(messageIn.getStringProperty(PROPERTY_TYPE_PREFIX + key.substring(PROPERTY_PREFIX.length()))));
                }
            }

            for (int i = 1; i <= numPayloads; i++) {
                transformToSubmissionHandlePayload(messageIn, target, bodyloadEnabled, i);
            }
        } catch (final JMSException ex) {
            LOG.error("Error while getting properties from MapMessage", ex);
            throw new DefaultJmsPluginException(ex);
        }
        return target;
    }

    private void setTargetToPartyIdAndToPartyType(MapMessage messageIn, Submission target) throws JMSException {
        String toPartyID = trim(messageIn.getStringProperty(TO_PARTY_ID));
        if (isEmpty(toPartyID)) {
            toPartyID = properties.getProperty(TO_PARTY_ID);
        }

        String toPartyType = trim(messageIn.getStringProperty(TO_PARTY_TYPE));
        if (isEmpty(toPartyType)) {
            toPartyType = properties.getProperty(TO_PARTY_TYPE);
        }
        target.addToParty(toPartyID, toPartyType);
    }

    private void setTargetFromPartyIdAndFromPartyType(MapMessage messageIn, Submission target) throws JMSException {
        String fromPartyID = trim(messageIn.getStringProperty(FROM_PARTY_ID));
        if (isEmpty(fromPartyID)) {
            fromPartyID = properties.getProperty(FROM_PARTY_ID);
        }

        String fromPartyType = trim(messageIn.getStringProperty(FROM_PARTY_TYPE));
        if (isEmpty(fromPartyType)) {
            fromPartyType = properties.getProperty(FROM_PARTY_TYPE);
        }
        target.addFromParty(fromPartyID, fromPartyType);
    }

    private void transformToSubmissionHandlePayload(MapMessage messageIn, Submission target, String bodyloadEnabled, int i) throws JMSException {
        final String propPayload = String.valueOf(MessageFormat.format(PAYLOAD_NAME_FORMAT, i));

        final String contentId;
        final String mimeType;
        final String fileName;
        String description = null;
        final String payMimeTypeProp = String.valueOf(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, i));
        mimeType = trim(messageIn.getStringProperty(payMimeTypeProp));
        final String payFileNameProp = String.valueOf(MessageFormat.format(PAYLOAD_FILE_NAME_FORMAT, i));
        fileName = trim(messageIn.getStringProperty(payFileNameProp));
        final String payDescrip = String.valueOf(MessageFormat.format(PAYLOAD_DESCRIPTION_FORMAT, i));
        if (messageIn.getStringProperty(payDescrip) != null) {
            description = trim(messageIn.getStringProperty(payDescrip));
        }
        final String payContID = String.valueOf(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, i));
        contentId = trim(messageIn.getStringProperty(payContID));
        final Collection<Submission.TypedProperty> partProperties = new ArrayList<>();
        if (mimeType != null && !mimeType.trim().equals("")) {
            partProperties.add(new Submission.TypedProperty(MIME_TYPE, mimeType));
        }
        if (fileName != null && !fileName.trim().equals("")) {
            partProperties.add(new Submission.TypedProperty(PAYLOAD_FILENAME, fileName));
        }
        DataHandler payloadDataHandler = null;
        try {
            payloadDataHandler = new DataHandler(new ByteArrayDataSource(messageIn.getBytes(propPayload), mimeType));
        } catch (JMSException jmsEx) {
            LOG.debug("no payload data as byte[] available, trying payload via URL", jmsEx);
            try {
                payloadDataHandler = new DataHandler(new URLDataSource(new URL(messageIn.getString(propPayload))));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(propPayload + " neither available as byte[] or URL, aborting transformation");
            }
        }
        boolean inBody = (i == 1 && "true".equalsIgnoreCase(bodyloadEnabled));

        String descriptionLanguage = trim(properties.getProperty(DESCRIPTION_LANGUAGE));
        Locale descriptionLocale = Locale.getDefault();
        if (!isEmpty(descriptionLanguage)) {
            try {
                descriptionLocale = new Locale(descriptionLanguage);
            } catch (RuntimeException rEx) {
                LOG.warn(DESCRIPTION_LANGUAGE + " could not be parsed. Using JVM locale", rEx);
            }
        }
        target.addPayload(contentId, payloadDataHandler, partProperties, inBody, new Submission.Description(descriptionLocale, description), null);
    }
}

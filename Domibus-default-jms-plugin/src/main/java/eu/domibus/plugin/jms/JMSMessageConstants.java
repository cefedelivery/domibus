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

package eu.domibus.plugin.jms;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class JMSMessageConstants {
    public static final String MESSAGE_ID = "messageId";
    public static final String P1_IN_BODY = "p1InBody";
    public static final String JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY = "messageType";
    public static final String MIME_TYPE = "MimeType";
    public static final String ACTION = "action";
    public static final String SERVICE = "service";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String CONVERSATION_ID = "conversationId";
    public static final String AGREEMENT_REF = "agreementRef";
    public static final String REF_TO_MESSAGE_ID = "refToMessageId";
    public static final String FROM_PARTY_ID = "fromPartyId";
    public static final String FROM_PARTY_TYPE = "fromPartyType";
    public static final String FROM_ROLE = "fromRole";
    public static final String TO_PARTY_ID = "toPartyId";
    public static final String TO_PARTY_TYPE = "toPartyType";
    public static final String TO_ROLE = "toRole";
    public static final String PROPERTY_ORIGINAL_SENDER = "originalSender";
    public static final String PROPERTY_FINAL_RECIPIENT = "finalRecipient";
    public static final String PROPERTY_ENDPOINT = "endPointAddress";
    public static final String PROTOCOL = "protocol";
    public static final String TOTAL_NUMBER_OF_PAYLOADS = "totalNumberOfPayloads";
    public static final String PAYLOAD_FILE_NAME_FORMAT = "payload_{0}.bin";
    public static final String BODYLOAD_FILE_NAME_FORMAT = "bodyload.bin";
    public static final String MESSAGE_TYPE_SUBMIT = "submitMessage";
    public static final String MESSAGE_TYPE_SUBMIT_RESPONSE = "submitResponse";
    public static final String MESSAGE_TYPE_INCOMING = "incomingMessage";
    public static final String MESSAGE_TYPE_SEND_SUCCESS = "messageSent";
    public static final String MESSAGE_TYPE_SEND_FAILURE = "messageSendFailure";
    public static final String MESSAGE_TYPE_RECEIVE_FAILURE = "messageReceptionFailure";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_DETAIL = "errorDetail";
    public static final String PROPERTY_PREFIX = "property_";
    public static final String PROPERTY_TYPE_PREFIX = "propertyType_";
    public static final String DESCRIPTION_LANGUAGE = "descriptionLanguage";
    private static final String PAYLOAD_NAME_PREFIX = "payload_";
    public static final String PAYLOAD_NAME_FORMAT = PAYLOAD_NAME_PREFIX + "{0}";
    private static final String PAYLOAD_DESCRIPTION_SUFFIX = "_description";
    public static final String PAYLOAD_DESCRIPTION_FORMAT = PAYLOAD_NAME_FORMAT + PAYLOAD_DESCRIPTION_SUFFIX;
    private static final String PAYLOAD_MIME_TYPE_SUFFIX = "_mimeType";
    public static final String PAYLOAD_MIME_TYPE_FORMAT = PAYLOAD_NAME_FORMAT + PAYLOAD_MIME_TYPE_SUFFIX;
    private static final String PAYLOAD_MIME_CONTENT_ID_SUFFIX = "_mimeContentId";
    public static final String PAYLOAD_MIME_CONTENT_ID_FORMAT = PAYLOAD_NAME_FORMAT + PAYLOAD_MIME_CONTENT_ID_SUFFIX;


}

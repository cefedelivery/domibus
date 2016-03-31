package eu.domibus.plugin.kerkovi;


import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Created by draguio on 25/02/2016.
 */
public class KerkoviAdapterImpl extends AbstractBackendConnector<Submission, Submission> {

    public static final String ACTION_SUBMIT = "Submit";
    public static final String ACTION_DELIVER = "Deliver";
    public static final String ACTION_NOTIFY = "Notify";
    public static final String PROP_MESSAGEID = "MessageId";
    public static final String PROP_CONVERSATIONID = "ConversationId";
    public static final String PROP_FROMPARTYID = "FromPartyId";
    public static final String PROP_FROMPARTYROLE = "FromPartyRole";
    public static final String PROP_TOPARTYID = "ToPartyId";
    public static final String PROP_TOPARTYROLE = "ToPartyRole";
    public static final String PROP_SERVICE = "Service";
    public static final String PROP_ACTION = "Action";
    public static final String PROP_ORIGINALSENDER = "originalSender";
    public static final String PROP_FINALRECIPIENT = "finalRecipient";
    public static final String PROP_SIGNALTYPE = "SignalType";
    public static final String PROP_SIGNALTYPE_RECEIPT = "Receipt";
    public static final String PROP_SIGNALTYPE_ERROR = "Error";
    public static final String PROP_ERRORCODE = "ErrorCode";
    public static final String PROP_SHORT_DESCRIPTION = "ShortDescription";
    public static final String PROP_DESCRIPTION = "Description";
    public static final String PROP_REFTOMESSAGEID = "RefToMessageId";
    public static final String CONFORMANCETEST_SERVICE = "http://www.esens.eu/as4/conformancetest";
    public static final String MINDER_ROLE = "http://esens.eu/as4/conformancetest/testdriver";
    public static final String SUT_ROLE = "http://esens.eu/as4/conformancetest/sut";
    private static final Log LOG = LogFactory.getLog(KerkoviAdapterImpl.class);
    private String partyIdDomibus;
    private String partyIdTypeDomibus;
    private String partyIdMinder;
    private String partyIdTypeMinder;
    @Autowired
    private MessageRetrievalTransformer<Submission> messageRetrievalTransformer;

    @Autowired
    private MessageSubmissionTransformer<Submission> messageSubmissionTransformer;

    public KerkoviAdapterImpl() {
        super("Kerkovi adapter");
    }

    @Override
    public MessageSubmissionTransformer<Submission> getMessageSubmissionTransformer() {
        return messageSubmissionTransformer;
    }

    @Override
    public MessageRetrievalTransformer<Submission> getMessageRetrievalTransformer() {
        return messageRetrievalTransformer;
    }

    @Override
    public void deliverMessage(final String messageId) {
        Submission request = null;
        LOG.info("Deliver message: " + messageId);
        try {
            LOG.info("Download message: " + messageId);
            request = super.downloadMessage(messageId, null);
            String action = request.getAction();
            LOG.info("Received action: " + action);
            if(request == null) {
                LOG.error("Failed to download the message " + messageId);
            }
            LOG.info("Message downloaded successfully " + messageId);
            LOG.info("Number of payloads" + request.getPayloads().size());
            switch (action) {
                case ACTION_SUBMIT:
                    handleSubmit(request);
                    break;
                default:
                    handleDeliver(request);

            }
        } catch (Exception e) {

            LOG.error("Error when delivering message! ", e);
        }
    }

    @Override
    public void messageSendSuccess(String messageId) {
        LOG.info("Handle messageSendSuccess for " + messageId);
        Submission toDeliver = new Submission();

        toDeliver.setAction(ACTION_NOTIFY);
        toDeliver.setService(CONFORMANCETEST_SERVICE);
        toDeliver.setFromRole(SUT_ROLE);
        toDeliver.setToRole(MINDER_ROLE);
        toDeliver.addFromParty(this.getPartyIdDomibus(), this.getPartyIdTypeDomibus());
        toDeliver.addToParty(this.getPartyIdMinder(), this.getPartyIdTypeMinder());

        toDeliver.addMessageProperty(PROP_SIGNALTYPE, PROP_SIGNALTYPE_RECEIPT);
        toDeliver.addMessageProperty(PROP_REFTOMESSAGEID, messageId);

        try {
            LOG.info("Submit " + toDeliver.getMessageId());
            super.submit(toDeliver);
        } catch (Exception e) {
            LOG.error("Error when notifying message! ", e);
        }
    }

    @Override
    public void messageSendFailed(String messageId) {
        LOG.info("Handle messageSendFailed for " + messageId);

        Submission toDeliver = new Submission();

        toDeliver.setAction(ACTION_NOTIFY);
        toDeliver.setService(CONFORMANCETEST_SERVICE);
        toDeliver.setFromRole(SUT_ROLE);
        toDeliver.setToRole(MINDER_ROLE);
        toDeliver.addFromParty(this.getPartyIdDomibus(), this.getPartyIdTypeDomibus());
        toDeliver.addToParty(this.getPartyIdMinder(), this.getPartyIdTypeMinder());

        toDeliver.addMessageProperty(PROP_SIGNALTYPE, PROP_SIGNALTYPE_ERROR);
        toDeliver.addMessageProperty(PROP_REFTOMESSAGEID, messageId);

        LOG.info("Add the last error for " + messageId);

        List<ErrorResult> errors = super.getErrorsForMessage(messageId);
        ErrorResult lastError = errors.get(errors.size() - 1);
        toDeliver.addMessageProperty(PROP_ERRORCODE, lastError.getErrorCode().getErrorCodeName());
        ErrorCode.EbMS3ErrorCode errorCode = ErrorCode.EbMS3ErrorCode.findErrorCodeBy(lastError.getErrorCode().getErrorCodeName());
        toDeliver.addMessageProperty(PROP_SHORT_DESCRIPTION, errorCode.getShortDescription());
        toDeliver.addMessageProperty(PROP_DESCRIPTION, lastError.getErrorDetail());

        try {
            LOG.info("Submit " + toDeliver.getMessageId());
            super.submit(toDeliver);
        } catch (Exception e) {
            LOG.error("Error when notifying message! ", e);
        }
    }

    private void handleDeliver(Submission request) throws Exception {
        LOG.info("Handle deliver " + request.getMessageId());

        Submission toDeliver = new Submission();
        toDeliver.setAction(ACTION_DELIVER);
        toDeliver.setService(CONFORMANCETEST_SERVICE);
        toDeliver.setFromRole(SUT_ROLE);
        toDeliver.setToRole(MINDER_ROLE);
        toDeliver.addFromParty(this.getPartyIdDomibus(), this.getPartyIdTypeDomibus());
        toDeliver.addToParty(this.getPartyIdMinder(), this.getPartyIdTypeMinder());

        toDeliver.addMessageProperty(PROP_MESSAGEID, request.getMessageId());
        toDeliver.addMessageProperty(PROP_CONVERSATIONID, request.getConversationId());

        toDeliver.addMessageProperty(PROP_FROMPARTYID, request.getFromParties().iterator().next().getPartyId());
        toDeliver.addMessageProperty(PROP_FROMPARTYROLE, request.getFromRole());
        toDeliver.addMessageProperty(PROP_TOPARTYID, request.getToParties().iterator().next().getPartyId());
        toDeliver.addMessageProperty(PROP_TOPARTYROLE, request.getToRole());
        toDeliver.addMessageProperty(PROP_ACTION, request.getAction());
        toDeliver.addMessageProperty(PROP_SERVICE, request.getService());
        for (Submission.TypedProperty typedProperty : request.getMessageProperties()) {
            if (PROP_ORIGINALSENDER.equals(typedProperty.getKey()) || PROP_FINALRECIPIENT.equals(typedProperty.getKey())) {
                toDeliver.addMessageProperty(typedProperty.getKey(), typedProperty.getValue(), typedProperty.getType());
            }
        }
        LOG.info("Add payloads for " + request.getMessageId());

        for (Submission.Payload payload : request.getPayloads()) {
            toDeliver.addPayload(payload);
        }

        LOG.info("Submit " + toDeliver.getMessageId());
        super.submit(toDeliver);

    }

    private void handleSubmit(Submission request) throws Exception {
        LOG.info("Handle Submit action on " + request.getMessageId());
        Submission toSubmit = new Submission();
        Collection<Submission.TypedProperty> properties = request.getMessageProperties();
        for (Submission.TypedProperty property : properties) {
            switch (property.getKey()) {
                case PROP_MESSAGEID:
                    toSubmit.setMessageId(property.getValue());
                    break;
                case PROP_CONVERSATIONID:
                    toSubmit.setConversationId(property.getValue());
                    break;
                case PROP_FROMPARTYROLE:
                    toSubmit.setFromRole(property.getValue());
                    break;
                case PROP_FROMPARTYID:
                    toSubmit.addFromParty(property.getValue(), null);
                    break;
                case PROP_TOPARTYROLE:
                    toSubmit.setToRole(property.getValue());
                    break;
                case PROP_TOPARTYID:
                    toSubmit.addToParty(property.getValue(), null);
                    break;
                case PROP_SERVICE:
                    toSubmit.setService(property.getValue());
                    break;
                case PROP_ACTION:
                    toSubmit.setAction(property.getValue());
                    break;
                case PROP_ORIGINALSENDER:
                    toSubmit.addMessageProperty(PROP_ORIGINALSENDER, property.getValue(), null);
                    break;
                case PROP_FINALRECIPIENT:
                    toSubmit.addMessageProperty(PROP_FINALRECIPIENT, property.getValue(), null);
                    break;
            }
        }
        LOG.info("Add payloads for " + request.getMessageId());
        for (Submission.Payload payload : request.getPayloads()) {
            toSubmit.addPayload(payload);
        }

        LOG.info("Submit " + toSubmit.getMessageId());
        super.submit(toSubmit);
    }

    @Override
    public void messageReceiveFailed(String messageId, String endpoint) {
        LOG.info("Incoming message was declined from  " + endpoint);
    }

    public String getPartyIdDomibus() {
        return partyIdDomibus;
    }

    public void setPartyIdDomibus(String partyIdDomibus) {
        this.partyIdDomibus = partyIdDomibus;
    }

    public String getPartyIdTypeDomibus() {
        return partyIdTypeDomibus;
    }

    public void setPartyIdTypeDomibus(String partyIdTypeDomibus) {
        this.partyIdTypeDomibus = partyIdTypeDomibus;
    }

    public String getPartyIdMinder() {
        return partyIdMinder;
    }

    public void setPartyIdMinder(String partyIdMinder) {
        this.partyIdMinder = partyIdMinder;
    }

    public String getPartyIdTypeMinder() {
        return partyIdTypeMinder;
    }

    public void setPartyIdTypeMinder(String partyIdTypeMinder) {
        this.partyIdTypeMinder = partyIdTypeMinder;
    }
}

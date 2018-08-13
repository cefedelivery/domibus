package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.core.message.SignalMessageLogDefaultService;
import eu.domibus.core.nonrepudiation.NonRepudiationService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 */
@Service
public class ResponseHandler {

    private static final DomibusLogger logger = DomibusLoggerFactory.getLogger(ResponseHandler.class);

    @Autowired
    @Qualifier("jaxbContextEBMS")
    private JAXBContext jaxbContext;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private NonRepudiationService nonRepudiationService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageLogDefaultService signalMessageLogDefaultService;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    public CheckResult handle(final SOAPMessage response) throws EbMS3Exception {

        final Messaging messaging;

        try {
            messaging = getMessaging(response);
        } catch (JAXBException | SOAPException ex) {
            logger.error("Error while un-marshalling message", ex);
            return CheckResult.UNMARSHALL_ERROR;
        }

        final SignalMessage signalMessage = messaging.getSignalMessage();
        nonRepudiationService.saveResponse(response, signalMessage);

        // Stores the signal message
        signalMessageDao.create(signalMessage);
        // Updating the reference to the signal message
        Messaging sentMessage = messagingDao.findMessageByMessageId(messaging.getSignalMessage().getMessageInfo().getRefToMessageId());
        String userMessageService = null;
        String userMessageAction = null;
        if (sentMessage != null) {
            userMessageService = sentMessage.getUserMessage().getCollaborationInfo().getService().getValue();
            userMessageAction = sentMessage.getUserMessage().getCollaborationInfo().getAction();
            sentMessage.setSignalMessage(signalMessage);
            messagingDao.update(sentMessage);
        }
        // Builds the signal message log
        signalMessageLogDefaultService.save(signalMessage.getMessageInfo().getMessageId(), userMessageService, userMessageAction);

        //UI replication
        uiReplicationSignalService.signalMessageReceived(signalMessage.getMessageInfo().getMessageId());

        // Checks if the signal message is Ok
        if (signalMessage.getError() == null || signalMessage.getError().isEmpty()) {
            return CheckResult.OK;
        }
        return handleErrors(signalMessage);

    }

    private CheckResult handleErrors(SignalMessage signalMessage) throws EbMS3Exception {
        //TODO: piggybacking support
        for (final Error error : signalMessage.getError()) {
            if (ErrorCode.SEVERITY_FAILURE.equalsIgnoreCase(error.getSeverity())) {
                EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.findErrorCodeBy(error.getErrorCode()), error.getErrorDetail(), error.getRefToMessageInError(), null);
                ebMS3Ex.setMshRole(MSHRole.SENDING);
                throw ebMS3Ex;
            }

            if (ErrorCode.SEVERITY_WARNING.equalsIgnoreCase(error.getSeverity())) {
                EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.findErrorCodeBy(error.getErrorCode()), error.getErrorDetail(), error.getRefToMessageInError(), null);

                final ErrorLogEntry errorLogEntry = new ErrorLogEntry(ebMS3Ex);
                this.errorLogDao.create(errorLogEntry);
            }
        }

        return CheckResult.WARNING;
    }


    public enum CheckResult {
        OK, WARNING, UNMARSHALL_ERROR
    }


    private Messaging getMessaging(final SOAPMessage soapMessage) throws SOAPException, JAXBException {
        final Node messagingXml = (Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        @SuppressWarnings("unchecked") final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }
}

package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.SignalMessageLogBuilder;
import eu.domibus.api.message.ebms3.model.Error;
import eu.domibus.api.message.ebms3.model.Messaging;
import eu.domibus.api.message.ebms3.model.ObjectFactory;
import eu.domibus.api.message.ebms3.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    public CheckResult handle(final SOAPMessage response) throws EbMS3Exception {

        final Messaging messaging;

        try {
            messaging = this.jaxbContext.createUnmarshaller().unmarshal((Node) response.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next(), Messaging.class).getValue();
        } catch (JAXBException | SOAPException ex) {
            logger.error("Error while un-marshalling message", ex);
            return CheckResult.UNMARSHALL_ERROR;
        }

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
                .setMessageId(signalMessage.getMessageInfo().getMessageId())
                .setMessageStatus(MessageStatus.RECEIVED)
                .setMshRole(MSHRole.RECEIVING)
                .setNotificationStatus(NotificationStatus.NOT_REQUIRED);
        // Saves an entry of the signal message log
        signalMessageLogDao.create(smlBuilder.build());
        // Checks if the signal message is Ok
        if (signalMessage.getError() == null || signalMessage.getError().size() == 0) {
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
}

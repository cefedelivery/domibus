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
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.common.model.logging.SignalMessageLogBuilder;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.util.SoapUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 */
@Service
public class ResponseHandler {

    private static final Log logger = LogFactory.getLog(ResponseHandler.class);

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
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    private MessagingDao messagingDao;

    public CheckResult handle(final SOAPMessage response) throws EbMS3Exception {

        final Messaging messaging;

        try {
            messaging = getMessaging(response);
        } catch (JAXBException | SOAPException ex) {
            logger.error("Unable to read message due to error: ", ex);
            return CheckResult.UNMARSHALL_ERROR;
        }

        final SignalMessage signalMessage = messaging.getSignalMessage();

        try {
            String rawXMLMessage = SoapUtil.getRawXMLMessage(response);
            logger.debug("Persist raw XML envelope: " + rawXMLMessage);
            RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
            rawEnvelopeLog.setRawXML(rawXMLMessage);
            rawEnvelopeLog.setSignalMessage(signalMessage);
            rawEnvelopeLogDao.create(rawEnvelopeLog);
        } catch (TransformerException e) {
            logger.warn("Unable to log the raw message XML due to: ", e);
        }

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


    private Messaging getMessaging(final SOAPMessage soapMessage) throws SOAPException, JAXBException {
        final Node messagingXml = (Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        @SuppressWarnings("unchecked") final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }
}

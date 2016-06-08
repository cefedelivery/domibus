package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.PModeProvider;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.soap.SOAPMessage;

/**
 * Created by muellers on 6/2/16.
 */
@Service(value = "signalMessageSenderService")
public class SignalMessageSender implements MessageListener {

    private static final Log LOG = LogFactory.getLog(SignalMessageSender.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private MSHDispatcher mshDispatcher;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Transactional(propagation = Propagation.REQUIRED)
    public void onMessage(final Message message) {

        String messageId = null;

        try {
            messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            sendSignalMessage(messageId);
        } catch (JMSException e) {
            LOG.error("", e);
        } catch (EbMS3Exception e) {
            if (message != null) {
                e.setRefToMessageId(messageId);
                if (!e.isRecoverable() && !Boolean.parseBoolean(System.getProperty(RetryService.UNRECOVERABLE_ERROR_RETRY))) {
                    LOG.error("Non recoverable error for SignalMessage with messageId " + messageId);
                }

                e.setMshRole(MSHRole.RECEIVING);
                LOG.error("Error for signal message with ID [" + messageId + "]", e);
                this.errorLogDao.create(new ErrorLogEntry(e));
                throw new RuntimeException(e);
            }
        }

    }

    private void sendSignalMessage(String messageId) throws EbMS3Exception {

        final SignalMessage signalMessage = this.messagingDao.findSignalMessageByMessageId(messageId);

        if (signalMessage == null) {
            EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "No SignalMessage found", messageId, null);
            e.setMshRole(MSHRole.RECEIVING);
            e.setRecoverable(false);
            throw e;
        }

        if (signalMessage.getMessageInfo() == null || signalMessage.getMessageInfo().getMessageId().isEmpty()) {
            EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "SignalMessage does not contain MessageInfo Element or does not have a messageId.", messageId, null);
            e.setMshRole(MSHRole.RECEIVING);
            e.setRecoverable(false);
            throw e;
        }

        final String refToMessageId = signalMessage.getMessageInfo().getRefToMessageId();

        if (refToMessageId == null) {
            EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "SignalMessage is not associated with a UserMessage (refToMessageId missing) and therefore no pmode for this message is found", messageId, null);
            e.setMshRole(MSHRole.RECEIVING);
            e.setRecoverable(false);
            throw e;
        }

        final UserMessage referencedMessage = this.messagingDao.findUserMessageByMessageId(refToMessageId);

        String pmodeKey = this.pModeProvider.findPModeKeyForUserMessage(referencedMessage);

        LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        String endpoint = this.pModeProvider.getSenderParty(pmodeKey).getEndpoint();
        final SOAPMessage soapMessage = this.messageBuilder.buildSOAPMessage(signalMessage, legConfiguration);
        this.mshDispatcher.dispatch(soapMessage, pmodeKey, endpoint, true);


    }


}

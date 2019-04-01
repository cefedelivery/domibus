package eu.domibus.ebms3.receiver.handler;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.metrics.Counter;
import eu.domibus.common.metrics.Timer;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.core.pull.PullRequestResult;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import org.apache.cxf.interceptor.Fault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;

/**
 * Handles the incoming AS4 pull receipt
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingPullReceiptHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingPullReceiptHandler.class);

    private static final String INCOMING_PULL_REQUEST_RECEIPT = "incoming_pull_request_receipt";

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    private ResponseHandler responseHandler;

    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Autowired
    private ReliabilityMatcher pullReceiptMatcher;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected SoapUtil soapUtil;

    @Override
    @Timer(INCOMING_PULL_REQUEST_RECEIPT)
    @Counter(INCOMING_PULL_REQUEST_RECEIPT)
    public SOAPMessage processMessage(SOAPMessage request, Messaging messaging) {
        LOG.trace("before pull receipt.");
        final SOAPMessage soapMessage = handlePullRequestReceipt(request, messaging);
        LOG.trace("returning pull receipt.");
        return soapMessage;
    }

    protected SOAPMessage handlePullRequestReceipt(SOAPMessage request, Messaging messaging) {
        String messageId = messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.PULL_FAILED;
        ResponseHandler.CheckResult isOk = null;
        LegConfiguration legConfiguration = null;
        UserMessage userMessage = null;
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (MessageStatus.WAITING_FOR_RECEIPT != userMessageLog.getMessageStatus()) {
            LOG.error("[PULL_RECEIPT]:Message:[{}] receipt a pull acknowledgement but its status is [{}]", userMessageLog.getMessageId(), userMessageLog.getMessageStatus());
            EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, String.format("No message in waiting for callback state found for receipt referring to :[%s]", messageId), messageId, null);
            return messageBuilder.getSoapMessage(ebMS3Exception);
        }
        LOG.debug("[handlePullRequestReceipt]:Message:[{}] delete lock ", messageId);

        final MessagingLock lock = pullMessageService.getLock(messageId);
        if (lock == null || MessageState.WAITING != lock.getMessageState()) {
            LOG.trace("Message[{}] could not acquire lock", messageId);
            LOG.error("[PULL_RECEIPT]:Message:[{}] time to receipt a pull acknowledgement has expired.", messageId);
            EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, String.format("Time to receipt a pull acknowledgement for message:[%s] has expired", messageId), messageId, null);
            return messageBuilder.getSoapMessage(ebMS3Exception);
        }

        try {
            userMessage = messagingDao.findUserMessageByMessageId(messageId);
            String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
            SOAPMessage soapMessage = getSoapMessage(messageId, legConfiguration, userMessage);
            isOk = responseHandler.handle(request);
            if (ResponseHandler.CheckResult.UNMARSHALL_ERROR.equals(isOk)) {
                EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "Problem occurred during marshalling", messageId, null);
                e.setMshRole(MSHRole.SENDING);
                throw e;
            }
            reliabilityCheckSuccessful = reliabilityChecker.check(soapMessage, request, pModeKey, pullReceiptMatcher);
        } catch (final SOAPFaultException soapFEx) {
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), messageId);
            } else {
                LOG.warn("[PULL_RECEIPT]:Error for message with ID [" + messageId + "]", soapFEx);
            }
        } catch (final EbMS3Exception e) {
            reliabilityChecker.handleEbms3Exception(e, messageId);
        } catch (ReliabilityException r) {
            LOG.warn(r.getMessage(), r);
        } finally {
            final PullRequestResult pullRequestResult = pullMessageService.updatePullMessageAfterReceipt(reliabilityCheckSuccessful, isOk, userMessageLog, legConfiguration, userMessage);
            pullMessageService.releaseLockAfterReceipt(pullRequestResult);
        }
        return null;
    }

    protected SOAPMessage getSoapMessage(String messageId, LegConfiguration legConfiguration, UserMessage userMessage) throws EbMS3Exception {
        SOAPMessage soapMessage;
        if (pullReceiptMatcher.matchReliableReceipt(legConfiguration.getReliability()) && legConfiguration.getReliability().isNonRepudiation()) {
            RawEnvelopeDto rawEnvelopeDto = messageExchangeService.findPulledMessageRawXmlByMessageId(messageId);
            try {
                soapMessage = soapUtil.createSOAPMessage(rawEnvelopeDto.getRawMessage());
            } catch (ParserConfigurationException | SOAPException | SAXException | IOException e) {
                throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "Raw message found in db but impossible to restore it");
            }
        } else {
            soapMessage = messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
        }
        return soapMessage;
    }

}

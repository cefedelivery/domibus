package eu.domibus.ebms3.receiver.handler;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptBuilder;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.DomibusCertificateException;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.sql.Timestamp;

/**
 * @author Thomas Dussart
 * @since 3.3
 */


@Component
public class PullRequestHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullRequestHandler.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;


    @Autowired
    private ReliabilityMatcher pullRequestMatcher;

    @Autowired
    private MessageAttemptService messageAttemptService;



    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private RetryService retryService;

    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Autowired
    private ReliabilityService reliabilityService;


    public SOAPMessage handlePullRequest(String messageId, PullContext pullContext) {
        if (messageId != null) {
            return handleRequest(messageId, pullContext);
        } else {
            return notifyNoMessage(pullContext);
        }
    }

    SOAPMessage notifyNoMessage(PullContext pullContext) {
        LOG.debug("No message for received pull request with mpc " + pullContext.getMpcQualifiedName());
        EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0006, "There is no message available for\n" +
                "pulling from this MPC at this moment.", null, null);
        final SignalMessage signalMessage = new SignalMessage();
        signalMessage.getError().add(ebMS3Exception.getFaultInfo());
        try {
            return messageBuilder.buildSOAPMessage(signalMessage, null);
        } catch (EbMS3Exception e) {
            try {
                return messageBuilder.buildSOAPFaultMessage(e.getFaultInfo());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        }
    }


    SOAPMessage handleRequest(String messageId, PullContext pullContext) {
        LegConfiguration leg = null;
        ReliabilityChecker.CheckResult checkResult = ReliabilityChecker.CheckResult.PULL_FAILED;
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.SUCCESS;
        String attemptError = null;
        final Timestamp startDate = new Timestamp(System.currentTimeMillis());
        boolean abortSending = false;
        SOAPMessage soapMessage = null;
        try {

            UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
            leg = pullContext.filterLegOnMpc();
            try {
                messageExchangeService.verifyReceiverCertificate(leg, pullContext.getInitiator().getName());
                messageExchangeService.verifySenderCertificate(leg, pullContext.getResponder().getName());
                leg = pullContext.filterLegOnMpc();
                soapMessage = messageBuilder.buildSOAPMessage(userMessage, leg);
                PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
                if (pullRequestMatcher.matchReliableCallBack(leg) &&
                        leg.getReliability().isNonRepudiation()) {
                    PhaseInterceptorChain.getCurrentMessage().getExchange().put(DispatchClientDefaultProvider.MESSAGE_ID, messageId);
                }
                checkResult = ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK;
                return soapMessage;
            } catch (DomibusCertificateException dcEx) {
                LOG.error(dcEx.getMessage(), dcEx);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, dcEx.getMessage(), null, dcEx);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            } catch (ConfigurationException e) {
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Policy configuration invalid", null, e);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

        } catch (ChainCertificateInvalidException e) {
            abortSending = true;
        } catch (EbMS3Exception e) {
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            reliabilityChecker.handleEbms3Exception(e, messageId);
            try {
                soapMessage = messageBuilder.buildSOAPFaultMessage(e.getFaultInfo());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        } catch (Throwable e) { // NOSONAR: This was done on purpose.
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            throw e;
        } finally {
            if (abortSending) {
                LOG.debug("Skipped checking the reliability for message [" + messageId + "]: message sending has been aborted");
                LOG.error("Cannot handle pullrequest for message: receiver " + pullContext.getInitiator().getName() + "  certificate is not valid or it has been revoked ");
                retryService.purgeTimedoutMessageInANewTransaction(messageId);
            } else {
                reliabilityService.handleReliability(messageId, checkResult, null, leg);
                try {
                    final MessageAttempt attempt = MessageAttemptBuilder.create()
                            .setMessageId(messageId)
                            .setAttemptStatus(attemptStatus)
                            .setAttemptError(attemptError)
                            .setStartDate(startDate).build();
                    messageAttemptService.create(attempt);
                } catch (Exception e) {
                    LOG.error("Could not create the message attempt", e);
                }
            }
        }
        return soapMessage;
    }

}

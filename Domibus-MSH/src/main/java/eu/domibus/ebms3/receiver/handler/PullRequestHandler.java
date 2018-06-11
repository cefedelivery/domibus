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
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.DomibusCertificateException;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.sql.Timestamp;

import static eu.domibus.ebms3.sender.ReliabilityChecker.CheckResult.ABORT;
import static eu.domibus.ebms3.sender.ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK;

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
    private ReliabilityChecker reliabilityChecker;

    @Autowired
    private PullMessageService pullMessageService;

    public SOAPMessage handlePullRequest(String messageId, PullContext pullContext) {
        if (messageId != null) {
            LOG.info("Message id [{}] ", messageId);
            return handleRequest(messageId, pullContext);
        } else {
            return notifyNoMessage(pullContext);
        }
    }

    SOAPMessage notifyNoMessage(PullContext pullContext) {
        LOG.trace("No message for received pull request with mpc " + pullContext.getMpcQualifiedName());
        EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0006, "There is no message available for\n" +
                "pulling from this MPC at this moment.", null, null);
        return getSoapMessage(ebMS3Exception);
    }

    public SOAPMessage getSoapMessage(EbMS3Exception ebMS3Exception) {
        final SignalMessage signalMessage = new SignalMessage();
        signalMessage.getError().add(ebMS3Exception.getFaultInfoError());
        try {
            return messageBuilder.buildSOAPMessage(signalMessage, null);
        } catch (EbMS3Exception e) {
            try {
                return messageBuilder.buildSOAPFaultMessage(e.getFaultInfoError());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        }
    }

    @Transactional
    SOAPMessage handleRequest(String messageId, PullContext pullContext) {
        LegConfiguration leg = null;
        ReliabilityChecker.CheckResult checkResult = ReliabilityChecker.CheckResult.PULL_FAILED;
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.SUCCESS;
        String attemptError = null;
        final Timestamp startDate = new Timestamp(System.currentTimeMillis());
        SOAPMessage soapMessage = null;
        UserMessage userMessage = null;
        try {
            userMessage = messagingDao.findUserMessageByMessageId(messageId);
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
                checkResult = WAITING_FOR_CALLBACK;
                LOG.info("Sending message");
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
            checkResult = ABORT;
            LOG.debug("Skipped checking the reliability for message [" + messageId + "]: message sending has been aborted");
            LOG.error("Cannot handle pullrequest for message: receiver " + pullContext.getInitiator().getName() + "  certificate is not valid or it has been revoked ");
        } catch (EbMS3Exception e) {
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            reliabilityChecker.handleEbms3Exception(e, messageId);
            try {
                soapMessage = messageBuilder.buildSOAPFaultMessage(e.getFaultInfoError());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        } catch (Throwable e) { // NOSONAR: This was done on purpose.
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            throw e;
        } finally {
            LOG.debug("Before updatePullMessageAfterRequest message id[{}] checkResult[{}]", messageId, checkResult);
            pullMessageService.updatePullMessageAfterRequest(userMessage, messageId, leg, checkResult);
            if (checkResult != ABORT) {
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

package eu.domibus.ebms3.receiver.handler;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.common.util.MessageAttemptUtil;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.MSHWebservice;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.DomibusCertificateException;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.sql.Timestamp;

/**
 * @author Thomas Dussart
 * @since 3.3
 */


@Component
public class PullRequestHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebservice.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;


    @Autowired
    private ReliabilityMatcher pullRequestMatcher;

    @Autowired
    private MessageAttemptService messageAttemptService;

    @Autowired
    private MessageAttemptUtil messageAttemptUtil;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SOAPMessage handlePullRequestInNewTransaction(String messageId, PullContext pullContext) {
        if (messageId != null) {
            return handleRequest(messageId, pullContext);
        } else {
            return notifyNoMessage(pullContext);
        }
    }

    private SOAPMessage notifyNoMessage(PullContext pullContext) {
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
        ReliabilityChecker.CheckResult checkResult = ReliabilityChecker.CheckResult.FAIL;
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.SUCCESS;
        String attemptError = null;
        final Timestamp startDate = new Timestamp(System.currentTimeMillis());
        boolean abortSending = false;
        try {
            UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
            try {
                abortSending = !messageExchangeService.areMessagePartiesCertificatesValid(userMessage);
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

            leg = pullContext.filterLegOnMpc();
            SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(userMessage, leg);
            PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
            if (pullRequestMatcher.matchReliableCallBack(leg) &&
                    leg.getReliability().isNonRepudiation()) {
                PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_ID, messageId);
            }
            checkResult = ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK;
            return soapMessage;

        } catch (EbMS3Exception e) {
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            try {
                return messageBuilder.buildSOAPFaultMessage(e.getFaultInfo());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        } catch (Throwable e) {
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            throw e;
        } finally {
            if (abortSending) {
                LOG.debug("Skipped checking the reliability for message [" + messageId + "]: message sending has been aborted");
            } else {
                messageExchangeService.handleReliability(messageId, checkResult, null, leg);
                try {
                    final MessageAttempt attempt = messageAttemptUtil.saveMessageAttempt(messageId, attemptStatus, attemptError, startDate);
                    messageAttemptService.create(attempt);
                } catch (Exception e) {
                    LOG.error("Could not create the message attempt", e);
                }
            }
        }
    }
}

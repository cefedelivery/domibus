package eu.domibus.ebms3.receiver.handler;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.impl.PullContext;
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
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;

/**
 * @author Thomas Dussart
 * @since 3.3
 */


public class PullRequestHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebservice.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Autowired
    private ReliabilityMatcher pullRequestMatcher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SOAPMessage handlePullRequestInNewTransaction(String messageId, PullContext pullContext) {
        return handlePullRequest(messageId, pullContext);
    }

    public SOAPMessage handlePullRequest(String messageId, PullContext pullContext) {
        LegConfiguration leg = null;
        try {
            if (messageId != null) {
                UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
                leg = pullContext.filterLegOnMpc();
                SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(userMessage, leg);
                PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
                if (pullRequestMatcher.matchReliableCallBack(leg) &&
                        leg.getReliability().isNonRepudiation()) {
                    PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_ID, messageId);
                }
                reliabilityChecker.handleReliability(messageId, ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK, null, leg);
                return soapMessage;
            } else {
                LOG.debug("No message for received pull request with mpc " + pullContext.getMpcQualifiedName());
                EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0006, "There is no message available for\n" +
                        "pulling from this MPC at this moment.", null, null);
                final SignalMessage signalMessage = new SignalMessage();
                signalMessage.getError().add(ebMS3Exception.getFaultInfo());
                SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(signalMessage, null);
                return soapMessage;
            }
        } catch (EbMS3Exception e) {
            reliabilityChecker.handleReliability(messageId, ReliabilityChecker.CheckResult.FAIL, null, leg);
            try {
                return messageBuilder.buildSOAPFaultMessage(e.getFaultInfo());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        }
    }
}

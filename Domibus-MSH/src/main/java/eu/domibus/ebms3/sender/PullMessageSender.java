package eu.domibus.ebms3.sender;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.common.DomibusInitializationHelper;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.core.pull.PullReceiptSender;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.receiver.UserMessageHandlerContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.pki.PolicyService;
import eu.domibus.util.MessageUtil;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Jms listener in charge of sending pullrequest.
 */
@Component
public class PullMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullMessageSender.class);

    @Autowired
    private MSHDispatcher mshDispatcher;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Qualifier("jaxbContextEBMS")
    @Autowired
    private JAXBContext jaxbContext;

    @Autowired
    private UserMessageHandlerService userMessageHandlerService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private DomibusInitializationHelper domibusInitializationHelper;

    @Autowired
    private PullReceiptSender pullReceiptSender;

    @SuppressWarnings("squid:S2583") //TODO: SONAR version updated!
    @JmsListener(destination = "${domibus.jms.queue.pull}", containerFactory = "pullJmsListenerContainerFactory")
    @Transactional(propagation = Propagation.REQUIRED)
    public void processPullRequest(final MapMessage map) {
        if (domibusInitializationHelper.isNotReady()) {
            return;
        }
        LOG.debug("Initiate pull request");
        boolean notifiyBusinessOnError = false;
        Messaging messaging = null;
        String messageId = null;
        try {
            final String mpc = map.getString(PullContext.MPC);
            final String pMode = map.getString(PullContext.PMODE_KEY);
            notifiyBusinessOnError = Boolean.valueOf(map.getString(PullContext.NOTIFY_BUSINNES_ON_ERROR));
            SignalMessage signalMessage = new SignalMessage();
            PullRequest pullRequest = new PullRequest();
            pullRequest.setMpc(mpc);
            signalMessage.setPullRequest(pullRequest);
            LOG.debug("Sending pull request with mpc " + mpc);
            final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pMode);
            final Party receiverParty = pModeProvider.getReceiverParty(pMode);
            final Policy policy;
            try {
                policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
            } catch (final ConfigurationException e) {

                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Policy configuration invalid", null, e);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }
            LOG.trace("Build soap message");
            SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(signalMessage, null);
            LOG.trace("Send soap message");
            final SOAPMessage response = mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pMode);
            messaging = MessageUtil.getMessage(response, jaxbContext);
            if (messaging.getUserMessage() == null && messaging.getSignalMessage() != null) {
                LOG.trace("No message for sent pull request with mpc " + mpc);
                logError(signalMessage);
                return;
            }
            messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
            UserMessageHandlerContext userMessageHandlerContext = new UserMessageHandlerContext();
            LOG.trace("handle message");
            final SOAPMessage acknowlegement = userMessageHandlerService.handleNewUserMessage(pMode, response, messaging, userMessageHandlerContext);
            final String sendMessageId = messageId;
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pullReceiptSender.sendReicept(acknowlegement, receiverParty.getEndpoint(), policy, legConfiguration, pMode, sendMessageId);
                    }
                }).start();
            } catch (Exception ex) {
                LOG.trace("Message[{}] exception while sending receipt assynchronously.");
            }
        } catch (TransformerException | SOAPException | IOException | JAXBException | JMSException e) {
            LOG.error(e.getMessage(), e);
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Error handling new UserMessage", e);
        } catch (final EbMS3Exception e) {
            try {
                if (notifiyBusinessOnError && messaging != null) {
                    backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), userMessageHandlerService.createErrorResult(e));
                }
            } catch (Exception ex) {
                LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, messageId);
            }
            checkConnectionProblem(e);
        }
    }


    private void logError(SignalMessage signalMessage) {
        Set<Error> error = signalMessage.getError();
        for (Error error1 : error) {
            LOG.info(error1.getErrorCode() + " " + error1.getShortDescription());
        }
    }

    private void checkConnectionProblem(EbMS3Exception e) {
        if (e.getErrorCode() == ErrorCode.EbMS3ErrorCode.EBMS_0005) {
            LOG.warn("ConnectionFailure ", e);
        } else {
            throw new WebServiceException(e);
        }
    }
}

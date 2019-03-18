package eu.domibus.ebms3.sender;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.DomibusInitializationHelper;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.pki.PolicyService;
import eu.domibus.util.MessageUtil;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executor;

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
    protected MessageUtil messageUtil;

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
    private UserMessageDefaultService userMessageDefaultService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor executor;

    @SuppressWarnings("squid:S2583") //TODO: SONAR version updated!
    @Transactional(propagation = Propagation.REQUIRED)
    //@TODO unit test this method.
    public void processPullRequest(final Message map) {
        if (domibusInitializationHelper.isNotReady()) {
            return;
        }
        String domainCode;
        try {
            domainCode = map.getStringProperty(MessageConstants.DOMAIN);
            domainContextProvider.setCurrentDomain(domainCode);
        } catch (JMSException e) {
            LOG.error("Could not get domain from pull request jms message:", e);
            return;
        }
        LOG.debug("Initiate pull request");
        boolean notifyBusinessOnError = false;
        Messaging messaging = null;
        String messageId = null;
        try {
            final String mpc = map.getStringProperty(PullContext.MPC);
            final String pModeKey = map.getStringProperty(PullContext.PMODE_KEY);
            notifyBusinessOnError = Boolean.valueOf(map.getStringProperty(PullContext.NOTIFY_BUSINNES_ON_ERROR));
            SignalMessage signalMessage = new SignalMessage();
            PullRequest pullRequest = new PullRequest();
            pullRequest.setMpc(mpc);
            signalMessage.setPullRequest(pullRequest);
            LOG.debug("Sending pull request with mpc:[{}]", mpc);
            final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            final Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
            final Policy policy = getPolicy(legConfiguration);
            LOG.trace("Build soap message");
            SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(signalMessage, null);
            LOG.trace("Send soap message");
            final SOAPMessage response = mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            messaging = messageUtil.getMessage(response);
            if (messaging.getUserMessage() == null && messaging.getSignalMessage() != null) {
                LOG.trace("No message for sent pull request with mpc:[{}]", mpc);
                logError(signalMessage);
                return;
            }
            messageId = messaging.getUserMessage().getMessageInfo().getMessageId();

            LOG.trace("handle message");
            Boolean testMessage = userMessageHandlerService.checkTestMessage(messaging.getUserMessage());
            userMessageHandlerService.handleNewUserMessage(legConfiguration, pModeKey, response, messaging, testMessage);
            final PartyInfo partyInfo = messaging.getUserMessage().getPartyInfo();
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED, partyInfo.getFrom().getFirstPartyId(), partyInfo.getTo().getFirstPartyId());
            String sendMessageId = messageId;
            if (userMessageHandlerService.checkSelfSending(pModeKey)) {
                sendMessageId += UserMessageHandlerService.SELF_SENDING_SUFFIX;
            }
            try {
                userMessageDefaultService.scheduleSendingPullReceipt(sendMessageId, pModeKey);
            } catch (Exception ex) {
                LOG.warn("Message[{}] exception while sending receipt asynchronously.", messageId, ex);
            }
        } catch (TransformerException | SOAPException | IOException | JAXBException | JMSException e) {
            LOG.error(e.getMessage(), e);
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Error handling new UserMessage", e);
        } catch (final EbMS3Exception e) {
            try {
                if (notifyBusinessOnError && messaging != null) {
                    backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), userMessageHandlerService.createErrorResult(e));
                }
            } catch (Exception ex) {
                LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, messageId);
            }
            checkConnectionProblem(e);
        }
    }

    private Policy getPolicy(LegConfiguration legConfiguration) throws EbMS3Exception {
        try {
            return policyService.getPolicy(legConfiguration);
        } catch (final ConfigurationException e) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Policy configuration invalid", null, e);
            ex.setMshRole(MSHRole.SENDING);
            throw ex;
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

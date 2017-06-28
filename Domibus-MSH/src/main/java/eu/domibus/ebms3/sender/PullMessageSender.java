package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.PModeDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.common.services.impl.UserMessageHandlerService;
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

    @JmsListener(destination = "${domibus.jms.queue.pull}", containerFactory = "internalJmsListenerContainerFactory")
    public void processPullRequest(final MapMessage map) {
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
            LOG.info("Sending message");
            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pMode);
            Party receiverParty = pModeProvider.getReceiverParty(pMode);
            Policy policy;
            try {
                policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
            } catch (final ConfigurationException e) {

                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Policy configuration invalid", null, e);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }
            SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(signalMessage, null);
            final SOAPMessage response = mshDispatcher.dispatch(soapMessage,receiverParty.getEndpoint(),policy,legConfiguration, pMode);
            messaging = MessageUtil.getMessage(response, jaxbContext);
            if(messaging.getUserMessage()==null && messaging.getSignalMessage()!=null){
                Set<Error> error = signalMessage.getError();
                //@thom why do not I have the error inside the message??
                LOG.info("No message for mpc "+mpc+" for the moment");
                for (Error error1 : error) {
                    LOG.info(error1.getErrorCode()+" "+error1.getShortDescription());
                }
                return;
            }
            messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
            UserMessageHandlerContext userMessageHandlerContext = new UserMessageHandlerContext();
            SOAPMessage acknowlegement = userMessageHandlerService.handleNewUserMessage(pMode, response, messaging, userMessageHandlerContext);
            //send receipt

            mshDispatcher.dispatch(acknowlegement,receiverParty.getEndpoint(),policy,legConfiguration, pMode);

        } catch (TransformerException | SOAPException | JAXBException | IOException | JMSException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (final EbMS3Exception e) {
            try {

                if (notifiyBusinessOnError && messaging != null) {
                    backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), userMessageHandlerService.createErrorResult(e));
                }
            } catch (Exception ex) {
                LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, messageId);
            }
            throw new WebServiceException(e);

        }
    }
}

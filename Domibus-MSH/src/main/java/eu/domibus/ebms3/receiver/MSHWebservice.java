package eu.domibus.ebms3.receiver;

import eu.domibus.common.*;
import eu.domibus.common.dao.*;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Leg;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.util.SoapUtil;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.ws.*;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.util.Date;

/**
 * This method is responsible for the receiving of ebMS3 messages and the sending of signal messages like receipts or ebMS3 errors in return
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@WebServiceProvider(portName = "mshPort", serviceName = "mshService")
@ServiceMode(Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MSHWebservice implements Provider<SOAPMessage> {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebservice.class);

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private MessagingDao messagingDao;

    private JAXBContext jaxbContext;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;
    @Autowired
    private UserMessageHandlerService userMessageHandlerService;
    @Autowired
    private ResponseHandler responseHandler;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    private ReliabilityChecker reliabilityChecker;

    public void setJaxbContext(final JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    @Override
    @Transactional
    public SOAPMessage invoke(final SOAPMessage request) {
        LOG.info("Receiving message");

        SOAPMessage responseMessage = null;
        Messaging messaging;
        messaging = getMessage(request);
        UserMessageHandlerContext userMessageHandlerContext = getMessageHandler();
        if (messaging.getSignalMessage() != null) {
            if (messaging.getSignalMessage().getPullRequest() != null) {
                return handlePullRequest(messaging);
            } else if (messaging.getSignalMessage().getReceipt() != null) {
                handlePullRequestReceipt(request, messaging);
            }
        } else {
            String pmodeKey = null;
            try {
                //FIXME: use a consistent way of property exchange between JAXWS and CXF message model. This: PropertyExchangeInterceptor
                pmodeKey = (String) request.getProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
            } catch (final SOAPException soapEx) {
                //this error should never occur because pmode handling is done inside the in-interceptorchain
                LOG.error("Cannot find PModeKey property for incoming Message", soapEx);
                assert false;
            }
            try {
                LOG.info("Using pmodeKey {}", pmodeKey);
                responseMessage = userMessageHandlerService.handleNewUserMessage(pmodeKey, request, messaging, userMessageHandlerContext);
                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED, userMessageHandlerContext.getMessageId());
                LOG.info("Ping message " + userMessageHandlerContext.isPingMessage());
            } catch (TransformerException | SOAPException | JAXBException | IOException e) {
                throw new RuntimeException(e);
            } catch (final EbMS3Exception e) {
                try {
                    System.out.println(e.getStackTrace());
                    System.out.println("messaging " + messaging);
                    System.out.println("userMessageHandlerContext.getLegConfiguration().getErrorHandling().isBusinessErrorNotifyConsumer() " + userMessageHandlerContext.getLegConfiguration().getErrorHandling().isBusinessErrorNotifyConsumer());
                    if (!userMessageHandlerContext.isPingMessage() && userMessageHandlerContext.getLegConfiguration().getErrorHandling().isBusinessErrorNotifyConsumer() && messaging != null) {
                        System.out.println("error " + messaging);
                        backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), userMessageHandlerService.createErrorResult(e));
                    }
                } catch (Exception ex) {
                    LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, userMessageHandlerContext.getMessageId());
                }
                throw new WebServiceException(e);
            }
        }

        return responseMessage;
    }

    UserMessageHandlerContext getMessageHandler() {
        return new UserMessageHandlerContext();
    }

    private SOAPMessage handlePullRequestReceipt(SOAPMessage request, Messaging messaging) {
        String messageId = messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = null;
        ResponseHandler.CheckResult isOk = null;
        LegConfiguration legConfiguration = null;
        try {
            final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
            String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.info("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
            SOAPMessage soapMessage = getSoapMessage(messageId, legConfiguration, userMessage);
            isOk = responseHandler.handle(request);
            if (ResponseHandler.CheckResult.UNMARSHALL_ERROR.equals(isOk)) {
                EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "Problem occurred during marshalling", messageId, null);
                e.setMshRole(MSHRole.SENDING);
                throw e;
            }
            reliabilityCheckSuccessful = reliabilityChecker.check(soapMessage, request, pModeKey);

        } catch (final SOAPFaultException soapFEx) {
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), messageId);
            } else {
                LOG.warn("Error for message with ID [" + messageId + "]", soapFEx);
            }
        } catch (final EbMS3Exception e) {
            reliabilityChecker.handleEbms3Exception(e, messageId);
        } catch (Throwable e) {
            throw e;
        } finally {
            reliabilityChecker.handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
            messageExchangeService.removeRawMessageIssuedByPullRequest(messageId);
            try {
                final SignalMessage signalMessage = new SignalMessage();
                return messageBuilder.buildSOAPMessage(signalMessage, null);
            } catch (EbMS3Exception e) {
                throw new WebServiceException(e);
            }
        }
    }

    private SOAPMessage getSoapMessage(String messageId, LegConfiguration legConfiguration, UserMessage userMessage) throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {
        SOAPMessage soapMessage;
        if (isNonRepudiation(legConfiguration)) {
            RawEnvelopeDto rawEnvelopeDto = messageExchangeService.findPulledMessageRawXmlByMessageId(messageId);
            soapMessage = SoapUtil.createSOAPMessage(rawEnvelopeDto.getRawMessage());
        } else {
            soapMessage = messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
        }
        return soapMessage;
    }

    private boolean isNonRepudiation(LegConfiguration legConfiguration) {
        return legConfiguration.getReliability() != null &&
                ReplyPattern.RESPONSE.equals(legConfiguration.getReliability().getReplyPattern()) &&
                legConfiguration.getReliability().isNonRepudiation();
    }

    private SOAPMessage handlePullRequest(Messaging messaging) {
        PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
        PullContext pullContext = messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
        if (!pullContext.isValid()) {
            throw new WebServiceException("Pmode configuration " + pullContext.createProcessWarningMessage());
        }
        UserMessage userMessage = messageExchangeService.retrieveReadyToPullUserMessages(pullContext.getMpcQualifiedName(), pullContext.getResponder());
        try {
            if (userMessage != null) {
                LegConfiguration leg = pullContext.filterLegOnMpc();
                SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(userMessage, leg);
                PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
                if (isNonRepudiation(leg)) {
                    PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_ID, userMessage.getMessageInfo().getMessageId());
                }
                return soapMessage;
            } else {
                LOG.info("No message for incoming request for mpc " + pullContext.getMpcQualifiedName());
                EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0006, "There is no message available for\n" +
                        "pulling from this MPC at this moment.", null, null);
                final SignalMessage signalMessage = new SignalMessage();
                signalMessage.getError().add(ebMS3Exception.getFaultInfo());
                SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(signalMessage, null);
                return soapMessage;
            }
        } catch (EbMS3Exception e) {
            throw new WebServiceException(e);
        }
    }

    private Messaging getMessage(SOAPMessage request) {
        Messaging messaging;
        try {
            messaging = getMessaging(request);
        } catch (SOAPException | JAXBException e) {
            throw new RuntimeException(e);
        }
        return messaging;
    }


    protected Messaging getMessaging(final SOAPMessage request) throws SOAPException, JAXBException {
        LOG.debug("Unmarshalling the Messaging instance from the request");
        return userMessageHandlerService.getMessaging(request);
    }

    public void deleteRawMessageIssuedByPullRequest() {

    }


}

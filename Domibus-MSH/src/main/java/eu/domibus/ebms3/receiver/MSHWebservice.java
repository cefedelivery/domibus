package eu.domibus.ebms3.receiver;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.matcher.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.handler.PullRequestHandler;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.util.SoapUtil;
import org.apache.cxf.interceptor.Fault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;

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

    @Autowired
    private ReliabilityMatcher pullReceiptMatcher;

    @Autowired
    private PullRequestHandler pullRequestHandler;


    public void setJaxbContext(final JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    @Override
    @Transactional
    public SOAPMessage invoke(final SOAPMessage request) {
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
            } catch (TransformerException | SOAPException | IOException e) {
                throw new UserMessageException(e);
            } catch (final EbMS3Exception e) {
                try {
                    if (!userMessageHandlerContext.isPingMessage() && userMessageHandlerContext.getLegConfiguration().getErrorHandling().isBusinessErrorNotifyConsumer()) {
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

    SOAPMessage handlePullRequestReceipt(SOAPMessage request, Messaging messaging) {
        String messageId = messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.FAIL;
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
            reliabilityCheckSuccessful = reliabilityChecker.check(soapMessage, request, pModeKey, pullReceiptMatcher);

        } catch (final SOAPFaultException soapFEx) {
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), messageId);
            } else {
                LOG.warn("Error for message with ID [" + messageId + "]", soapFEx);
            }
        } catch (final EbMS3Exception e) {
            reliabilityChecker.handleEbms3Exception(e, messageId);
        } catch (ReliabilityException r) {
            LOG.warn(r.getMessage());
        } finally {
            reliabilityChecker.handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
            messageExchangeService.removeRawMessageIssuedByPullRequest(messageId);
        }
        return null;
    }

    SOAPMessage getSoapMessage(String messageId, LegConfiguration legConfiguration, UserMessage userMessage) throws EbMS3Exception {
        SOAPMessage soapMessage;
        if (pullReceiptMatcher.matchReliableReceipt(legConfiguration) && legConfiguration.getReliability().isNonRepudiation()) {
            RawEnvelopeDto rawEnvelopeDto = messageExchangeService.findPulledMessageRawXmlByMessageId(messageId);
            try {
                soapMessage = SoapUtil.createSOAPMessage(rawEnvelopeDto.getRawMessage());
            } catch (ParserConfigurationException | SOAPException | SAXException | IOException e) {
                throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "Raw message found in db but impossible to restore it");
            }
        } else {
            soapMessage = messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
        }
        return soapMessage;
    }

    SOAPMessage handlePullRequest(Messaging messaging) {
        PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
        PullContext pullContext = messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
        String messageId = messageExchangeService.retrieveReadyToPullUserMessageId(pullContext.getMpcQualifiedName(), pullContext.getInitiator());
        return pullRequestHandler.handlePullRequestInNewTransaction(messageId, pullContext);
    }

    private Messaging getMessage(SOAPMessage request) {
        Messaging messaging;
        try {
            messaging = getMessaging(request);
        } catch (SOAPException | JAXBException e) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Problems getting message", e);
        }
        return messaging;
    }


    protected Messaging getMessaging(final SOAPMessage request) throws SOAPException, JAXBException {
        LOG.debug("Unmarshalling the Messaging instance from the request");
        return userMessageHandlerService.getMessaging(request);
    }


}

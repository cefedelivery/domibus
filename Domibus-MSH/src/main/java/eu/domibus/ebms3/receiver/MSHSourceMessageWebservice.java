package eu.domibus.ebms3.receiver;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.MessageUtil;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;


/**
 * Local endpoint used to generate the multi mimepart message for SplitAndJoin
 */
@WebServiceProvider(portName = "local-msh-dispatch", serviceName = "local-msh-dispatch-service")
@ServiceMode(Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MSHSourceMessageWebservice implements Provider<SOAPMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHSourceMessageWebservice.class);

    public static final String SOURCE_MESSAGE_FILE = "sourceMessageFile";

    @Autowired
    protected MessageFactory messageFactory;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @WebMethod
    @WebResult(name = "soapMessageResult")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SOAPMessage invoke(final SOAPMessage request) {
        LOG.debug("Processing SourceMessage request");

        final String domain = LOG.getMDC(MSHDispatcher.HEADER_DOMIBUS_DOMAIN);
        domainContextProvider.setCurrentDomain(domain);
        final Domain currentDomain = domainContextProvider.getCurrentDomain();

        final String contentTypeString = LOG.getMDC(Message.CONTENT_TYPE);
        final boolean compression = Boolean.valueOf(LOG.getMDC(MSHDispatcher.HEADER_DOMIBUS_SPLITTING_COMPRESSION));
        final String sourceMessageFileName = LOG.getMDC(MSHSourceMessageWebservice.SOURCE_MESSAGE_FILE);

        LOG.debug("Parsing the SourceMessage from file [{}]", sourceMessageFileName);

        SOAPMessage userMessageRequest = null;
        Messaging messaging = null;
        try {
            userMessageRequest = splitAndJoinService.getUserMessage(new File(sourceMessageFileName), contentTypeString);
            messaging = messageUtil.getMessaging(userMessageRequest);
        } catch (Exception e) {
            LOG.error("Error getting the Messaging object from the SOAPMessage", e);
            throw new WebServiceException(e);
        }
        LOG.debug("Finished parsing the SourceMessage from file [{}]", sourceMessageFileName);

        final UserMessage userMessage = messaging.getUserMessage();
        SOAPMessage finalUserMessageRequest = userMessageRequest;

        domainTaskExecutor.submitLongRunningTask(
                () -> splitAndJoinService.createUserFragmentsFromSourceFile(sourceMessageFileName, finalUserMessageRequest, userMessage, contentTypeString, compression),
                () -> splitAndJoinService.setSourceMessageAsFailed(userMessage),
                currentDomain);

        try {
            SOAPMessage responseMessage = messageFactory.createMessage();
            responseMessage.saveChanges();

            LOG.debug("Finished processing SourceMessage request");
            return responseMessage;
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }
}

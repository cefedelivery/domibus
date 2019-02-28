package eu.domibus.ebms3.receiver.handler;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Handles the incoming source message for SplitAndJoin mechanism
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingSourceMessageHandler extends AbstractIncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingSourceMessageHandler.class);

    @Override
    protected SOAPMessage processMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        LOG.debug("Processing SourceMessage");
        return userMessageHandlerService.handleNewSourceUserMessage(legConfiguration, pmodeKey, request, messaging, testMessage);
    }
}

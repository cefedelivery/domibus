package eu.domibus.ebms3.receiver.handler;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    protected StorageProvider storageProvider;

    @Override
    protected SOAPMessage processMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        LOG.debug("Processing SourceMessage");

        if (storageProvider.idPayloadsPersistenceInDatabaseConfigured()) {
            LOG.error("SplitAndJoin feature needs payload storage on the file system");
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0002, "SplitAndJoin feature needs payload storage on the file system", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        return userMessageHandlerService.handleNewSourceUserMessage(legConfiguration, pmodeKey, request, messaging, testMessage);
    }
}

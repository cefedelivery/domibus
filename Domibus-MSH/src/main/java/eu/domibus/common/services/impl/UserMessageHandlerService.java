package eu.domibus.common.services.impl;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface UserMessageHandlerService {
    /**
     * to be appended to messageId when saving to DB on receiver side
     */
    String SELF_SENDING_SUFFIX = "_1";

    SOAPMessage handleNewUserMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException;

    SOAPMessage handleNewSourceUserMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException;

    Boolean checkTestMessage(UserMessage message);

    Boolean checkTestMessage(final LegConfiguration legConfiguration);

    ErrorResult createErrorResult(EbMS3Exception ebm3Exception);
}

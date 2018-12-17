package eu.domibus.ebms3.sender;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class UserMessageSender extends AbstractUserMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageSender.class);


    @Override
    protected SOAPMessage createSOAPMessage(UserMessage userMessage, LegConfiguration legConfiguration) throws EbMS3Exception {
        return messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
    }

    @Override
    protected DomibusLogger getLog() {
        return LOG;
    }
}

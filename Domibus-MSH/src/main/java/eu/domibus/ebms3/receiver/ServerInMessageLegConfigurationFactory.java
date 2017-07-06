package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("serverInMessageLegConfigurationFactory")
public class ServerInMessageLegConfigurationFactory implements MessageLegConfigurationFactory {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ServerInMessageLegConfigurationFactory.class);

    @Autowired
    private UserMessageLegConfigurationFactory userMessageLegConfigurationFactory;

    @Autowired
    private PullRequestLegConfigurationFactory pullRequestLegConfigurationFactory;

    @Autowired
    private ReceiptLegConfigurationFactory receiptLegConfigurationFactory;


    @PostConstruct
    void init(){
        userMessageLegConfigurationFactory.
                chain(pullRequestLegConfigurationFactory).
                chain(receiptLegConfigurationFactory);

    }
    @Override
    public LegConfigurationExtractor extractMessageConfiguration(SoapMessage soapMessage, Messaging messaging) {
        LegConfigurationExtractor legConfigurationExtractor = userMessageLegConfigurationFactory.extractMessageConfiguration(soapMessage, messaging);
        if (legConfigurationExtractor == null) {
            LOG.error("Leconfiguration not found for incoming message with id " + messaging.getId());
        }
        return legConfigurationExtractor;
    }
}

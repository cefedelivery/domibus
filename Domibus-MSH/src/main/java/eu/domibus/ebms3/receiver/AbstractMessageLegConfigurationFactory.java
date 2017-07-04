package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public abstract class AbstractMessageLegConfigurationFactory implements MessageLegConfigurationFactory {

    private AbstractMessageLegConfigurationFactory next;

    private MessageLegConfigurationVisitor messageLegConfigurationVisitor;

    public AbstractMessageLegConfigurationFactory chain(AbstractMessageLegConfigurationFactory next) {
        this.next = next;
        return this.next;
    }

    public LegConfigurationExtractor extractMessageConfiguration(final SoapMessage soapMessage, final Messaging messaging) {
        LegConfigurationExtractor configuration = getConfiguration(soapMessage, messaging);
        if (configuration == null) {
            configuration = executeNextFactory(soapMessage, messaging);
        } else {
            configuration.accept(messageLegConfigurationVisitor);
        }
        return configuration;
    }

    abstract protected LegConfigurationExtractor getConfiguration(final SoapMessage soapMessage, final Messaging messaging);


    private LegConfigurationExtractor executeNextFactory(SoapMessage soapMessage, Messaging messaging) {
        if (next != null) {
            return next.extractMessageConfiguration(soapMessage, messaging);
        } else {
            return null;
        }
    }

    @Autowired
    void setMessageLegConfigurationVisitor(MessageLegConfigurationVisitor messageLegConfigurationVisitor) {
        this.messageLegConfigurationVisitor = messageLegConfigurationVisitor;
    }
}

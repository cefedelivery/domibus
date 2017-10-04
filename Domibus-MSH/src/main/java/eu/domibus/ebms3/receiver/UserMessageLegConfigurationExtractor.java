package eu.domibus.ebms3.receiver;

import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.sender.MSHDispatcher;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 * Loading legconfiguration for an incoming usermessage.
 */

public class UserMessageLegConfigurationExtractor extends AbstractLegConfigurationExtractor {

    private PModeProvider pModeProvider;

    UserMessageLegConfigurationExtractor(SoapMessage message, Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return messaging.getUserMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration extractMessageConfiguration() throws EbMS3Exception {
        message.put(MSHDispatcher.MESSAGE_TYPE_IN, MessageType.USER_MESSAGE);
        final String pmodeKey = this.pModeProvider.findUserMessageExchangeContext(messaging.getUserMessage(), MSHRole.RECEIVING).getPmodeKey(); // FIXME: This does not work for signalmessages
        setUpMessage(pmodeKey);
        return this.pModeProvider.getLegConfiguration(pmodeKey);
    }

    @Override
    public void accept(MessageLegConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    public void setpModeProvider(PModeProvider pModeProvider) {
        this.pModeProvider = pModeProvider;
    }
}

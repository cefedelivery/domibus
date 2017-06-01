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
 * Created by dussath on 5/31/17.
 */

public class UserMessagePolicyInSetup extends AbstractMessagePolicyInSetup {


    private PModeProvider pModeProvider;

    public UserMessagePolicyInSetup(SoapMessage message, Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return messaging.getUserMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration extractMessageConfiguration() throws EbMS3Exception {
        message.put(MSHDispatcher.MESSAGE_TYPE, MessageType.USER_MESSAGE);
        addMessageId();
        final String pmodeKey = this.pModeProvider.findUserMessageExchangeContext(messaging.getUserMessage(), MSHRole.RECEIVING).getPmodeKey(); // FIXME: This does not work for signalmessages
        LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        message.put(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey);
        //FIXME: Test!!!!
        message.getExchange().put(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey);
        return legConfiguration;
    }

    @Override
    public void accept(PolicyInSetupVisitor visitor) {
        visitor.visit(this);
    }

    public void setpModeProvider(PModeProvider pModeProvider) {
        this.pModeProvider = pModeProvider;
    }
}

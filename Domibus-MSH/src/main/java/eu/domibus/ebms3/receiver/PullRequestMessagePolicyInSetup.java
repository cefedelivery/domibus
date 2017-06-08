package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.context.MessageExchangeContext;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.sender.MSHDispatcher;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * Created by dussath on 5/31/17.
 */

public class PullRequestMessagePolicyInSetup extends AbstractSignalMessagePolicyInSetup{
    private MessageExchangeService messageExchangeService;

    public PullRequestMessagePolicyInSetup(SoapMessage message, Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return messaging.getSignalMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration process() throws EbMS3Exception {
        message.put(MSHDispatcher.MESSAGE_TYPE_IN, MessageType.SIGNAL_MESSAGE);
        PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
        PullContext pullContext = messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
        if (!pullContext.isValid()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Incoming pull request :" + pullContext.createProcessWarningMessage(), messaging != null ? getMessageId() : "unknown", null);
        }
        LegConfiguration legConfiguration = pullContext.getProcess().getLegs().iterator().next();
        MessageExchangeContext messageExchangeContext = new MessageExchangeContext(pullContext.getAgreement(),
                pullContext.getInitiator().getName(),
                pullContext.getResponder().getName(),
                legConfiguration.getService().getName(),
                legConfiguration.getAction().getName(),
                legConfiguration.getName());
        setUpMessage(messageExchangeContext.getPmodeKey());
        return legConfiguration;
    }

    @Override
    public void accept(PolicyInSetupVisitor visitor) {
        visitor.visit(this);
    }

    void setMessageExchangeService(MessageExchangeService messageExchangeService) {
        this.messageExchangeService = messageExchangeService;
    }
}

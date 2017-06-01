package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.sender.MSHDispatcher;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * Created by dussath on 5/31/17.
 */

public class PullRequestMessagePolicyInSetup extends AbstractMessagePolicyInSetup {
    private MessageExchangeService messageExchangeService;

    public PullRequestMessagePolicyInSetup(SoapMessage message, Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return messaging.getSignalMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration extractMessageConfiguration() throws EbMS3Exception {
        message.put(MSHDispatcher.MESSAGE_TYPE, MessageType.SIGNAL_MESSAGE);
        addMessageId();
        PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
        /**
         @question I can not retrieve the party before the policy interceptor. Meaning I can retrieve the policy only on Pull/MPC.
         that means that I will not authorize 2 pull process with the same mpc.
         *
         */
        PullContext pullContext = messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
        if (!pullContext.isValid()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Incoming pull request :" + pullContext.createWarningMessageForIncomingPullRequest(), messaging != null ? getMessageId() : "unknown", null);
        }
        return pullContext.getProcess().getLegs().iterator().next();
    }

    @Override
    public void accept(PolicyInSetupVisitor visitor) {
        visitor.visit(this);
    }

    void setMessageExchangeService(MessageExchangeService messageExchangeService) {
        this.messageExchangeService = messageExchangeService;
    }
}

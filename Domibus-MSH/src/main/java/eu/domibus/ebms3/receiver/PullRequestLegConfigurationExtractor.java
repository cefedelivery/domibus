package eu.domibus.ebms3.receiver;

import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.sender.MSHDispatcher;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class PullRequestLegConfigurationExtractor extends AbstractSignalLegConfigurationExtractor {
    private MessageExchangeService messageExchangeService;

    PullRequestLegConfigurationExtractor(SoapMessage message, Messaging messaging) {
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
        try {
            PullContext pullContext = messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
            LegConfiguration legConfiguration = pullContext.getProcess().getLegs().iterator().next();
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(pullContext.getAgreement(),
                    pullContext.getInitiator().getName(),
                    pullContext.getInitiator().getName(),
                    legConfiguration.getService().getName(),
                    legConfiguration.getAction().getName(),
                    legConfiguration.getName());
            setUpMessage(messageExchangeConfiguration.getPmodeKey());
            return legConfiguration;
        } catch (PModeException p) {
            EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Error for pullrequest with mpc:" + pullRequest.getMpc() + " " + p.getMessage(), null, null);
            LOG.warn(ebMS3Exception.getErrorDetail());
            throw ebMS3Exception;
        }
    }

    @Override
    public void accept(MessageLegConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    void setMessageExchangeService(MessageExchangeService messageExchangeService) {
        this.messageExchangeService = messageExchangeService;
    }
}

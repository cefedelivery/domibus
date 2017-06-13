package eu.domibus.ebms3.receiver;

import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * Created by dussath on 5/31/17.
 */

public class ReceiptMessagePolicyInSetup extends AbstractSignalMessagePolicyInSetup {

    private MessagingDao messagingDao;
    private PModeProvider pModeProvider;

    public ReceiptMessagePolicyInSetup(SoapMessage message, Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return messaging.getSignalMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration process() throws EbMS3Exception {
        LOG.debug("Extracting configuration for receipt");
        String messageId = messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
        setUpMessage(pModeKey);
        LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        return legConfiguration;
    }

    @Override
    public void accept(PolicyInSetupVisitor visitor) {
        visitor.visit(this);
    }


    public void setMessagingDao(MessagingDao messagingDao) {
        this.messagingDao = messagingDao;
    }

    public void setpModeProvider(PModeProvider pModeProvider) {
        this.pModeProvider = pModeProvider;
    }
}

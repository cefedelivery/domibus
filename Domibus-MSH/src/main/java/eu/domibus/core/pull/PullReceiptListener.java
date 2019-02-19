package eu.domibus.core.pull;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.pki.PolicyService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.soap.SOAPMessage;
import java.util.List;

/**
 * @author idragusa
 * @since 4.1
 */
@Service(value = "pullReceiptListener")
public class PullReceiptListener implements MessageListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullReceiptListener.class);

    @Autowired
    protected PullReceiptSender pullReceiptSender;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected PolicyService policyService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;


    @Autowired
    private SignalMessageDao signalMessageDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        try {
            String domainCode = null;
            try {
                domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            } catch (final JMSException e) {
                LOG.error("Error processing JMS message", e);
            }
            if (StringUtils.isBlank(domainCode)) {
                LOG.error("Domain is empty: could not send message");
                return;
            }

            domainContextProvider.setCurrentDomain(domainCode);
            final String refToMessageId = message.getStringProperty(UserMessageService.PULL_RECEIPT_REF_TO_MESSAGE_ID);
            final String pModeKey = message.getStringProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
            final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            final Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
            final Policy policy = policyService.getPolicy(legConfiguration);
            List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(refToMessageId);

            if (!CollectionUtils.isEmpty(signalMessages)) {
                for (SignalMessage signalMessage : signalMessages) {
                    if (signalMessage.getReceipt() != null && signalMessage.getReceipt().getAny().size() == 1) {
                        SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(signalMessage, legConfiguration);
                        pullReceiptSender.sendReceipt(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey, refToMessageId,domainCode);
                        break;
                    }

                }
            }
        } catch (final JMSException | EbMS3Exception e) {
            LOG.error("Error processing JMS message", e);
        }
        LOG.debug("[PullReceiptListener] ~~~ The end of onMessage ~~~");
    }
}

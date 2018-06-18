package eu.domibus.core.alerts;

import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.alerts.dao.MessageEventDao;
import eu.domibus.core.alerts.model.Event;
import eu.domibus.core.alerts.model.EventPropertyValue;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EventServiceImpl implements EventService{

    private final static Logger LOG = LoggerFactory.getLogger(EventServiceImpl.class);

    @Autowired
    private MessageEventDao messageEventDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Override
    public void enrichMessageEvent(Event event) {
        final EventPropertyValue message_id = event.getProperties().get("MESSAGE_ID");
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(message_id.getValue());
        final MSHRole mshRole = messageEvent.getMshRole();
        try {
            final MessageExchangeConfiguration userMessageExchangeContext =
                    pModeProvider.findUserMessageExchangeContext(userMessage, mshRole);
            final Party senderParty = pModeProvider.getSenderParty(userMessageExchangeContext.getPmodeKey());
            final Party receiverParty = pModeProvider.getReceiverParty(userMessageExchangeContext.getPmodeKey());
            messageEvent.setFromParty(senderParty.getName());
            messageEvent.setToParty(receiverParty.getName());
            errorLogDao.
                    getErrorsForMessage(messageEvent.getMessageId()).
                    stream().
                    map(errorLogEntry -> errorLogEntry.getErrorDetail()).
                    forEach(errorDetail -> messageEvent.addDescription(errorDetail));
            messageEventDao.create(messageEvent);
        } catch (EbMS3Exception e) {
            e.printStackTrace();
        }


        //enrich
    }
}

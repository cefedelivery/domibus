package eu.domibus.core.alerts;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.Event;
import eu.domibus.core.alerts.model.EventPropertyValue;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

import java.util.Map;
import java.util.Set;

import static eu.domibus.core.alerts.AlertEventType.MESSAGE;
import static eu.domibus.core.alerts.model.MessageEvent.*;

@Service
public class EventServiceImpl implements EventService {

    private final static Logger LOG = LoggerFactory.getLogger(EventServiceImpl.class);

    @Autowired
    private EventDao eventDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private JMSManager jmsManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("alertMessageQueue")
    private Queue alertMessageQueue;

    @Override
    public void sendMessageEvent(String messageId, MessageStatus oldStatus, MessageStatus newStatus, MSHRole role) {
        //check is status is relevant.
        Event event = new Event(MESSAGE.name());
        event.addKeyValue(OLD_STATUS.name(), oldStatus.name());
        event.addKeyValue(NEW_STATUS.name(), newStatus.name());
        event.addKeyValue(MESSAGE_ID.name(), messageId);
        event.addKeyValue(ROLE.name(), role.name());
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, "message");

        //send event to queue.

    }

    @Override
    public void persistEvent(final Event event) {
        final eu.domibus.core.alerts.model.persist.Event eventEntity = domainConverter.convert(event, eu.domibus.core.alerts.model.persist.Event.class);
        LOG.debug("Converting jms event[{}] to persistent event[{}]", event, eventEntity);
        event.getProperties().entrySet().forEach(entry -> {
            final eu.domibus.core.alerts.model.persist.EventPropertyValue propertyValue = domainConverter.convert(entry.getValue(), eu.domibus.core.alerts.model.persist.EventPropertyValue.class);
            final String key = entry.getKey();
            eventEntity.addProperty(key, propertyValue);
            LOG.debug("Transferring key[{}] value[{}] from jms event to persistent event", key);
        });
        eventDao.create(eventEntity);
        event.setEntityId(eventEntity.getEntityId());
    }

    @Override
    public void enrichMessage(final Event event) {
        final String messageId = event.findProperty(MESSAGE_ID.name()).get();
        final String role = event.findProperty(ROLE.name()).get();
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        final MessageExchangeConfiguration userMessageExchangeContext;
        try {
            userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.valueOf(role));
            final Party senderParty = pModeProvider.getSenderParty(userMessageExchangeContext.getPmodeKey());
            final Party receiverParty = pModeProvider.getReceiverParty(userMessageExchangeContext.getPmodeKey());
            event.addKeyValue(FROM_PARTY.name(), senderParty.getName());
            event.addKeyValue(TO_PARTY.name(), receiverParty.getName());
            StringBuilder stringBuilder = new StringBuilder();
            errorLogDao.
                    getErrorsForMessage(messageId).
                    stream().
                    map(ErrorLogEntry::getErrorDetail).forEach(error -> stringBuilder.append(error));
            event.addKeyValue(DESCRIPTION.name(), stringBuilder.toString());
        } catch (EbMS3Exception e) {
            LOG.error("Message:[{}] Errors while enriching message event", messageId, e);
        }
    }

/*    @Override
    public Event enrichMessageEvent(final Event event) {

        final String messageId = event.findProperty(MESSAGE_ID.name()).get();
        final String role = event.findProperty(ROLE.name()).get();
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        // final UserMessageLog byMessageId = userMessageLogDao.findByMessageId(messageId);
        try {


            final eu.domibus.core.alerts.model.persist.Event eventEntity = domainConverter.convert(event, eu.domibus.core.alerts.model.persist.Event.class);
            LOG.debug("Converting jms event[{}] to persistent event[{}]", event, eventEntity);
            final Set<Map.Entry<String, EventPropertyValue>> entries = event.getProperties().entrySet();
            for (Map.Entry<String, EventPropertyValue> entry : entries) {
                final eu.domibus.core.alerts.model.persist.EventPropertyValue propertyValue = domainConverter.convert(entry.getValue(), eu.domibus.core.alerts.model.persist.EventPropertyValue.class);
                final String key = entry.getKey();
                eventEntity.addProperty(key, propertyValue);
                LOG.debug("Transferring key[{}] value[{}] from jms event to persistent event", key);
            }
            eventEntity.addProperty(FROM_PARTY.name(), new eu.domibus.core.alerts.model.persist.EventPropertyValue(senderParty.getName()));
            eventEntity.addProperty(TO_PARTY.name(), new eu.domibus.core.alerts.model.persist.EventPropertyValue(receiverParty.getName()));

            StringBuilder stringBuilder = new StringBuilder();
            errorLogDao.
                    getErrorsForMessage(messageId).
                    stream().
                    map(ErrorLogEntry::getErrorDetail).forEach(error -> stringBuilder.append(error));
            event.addKeyValue(DESCRIPTION.name(), stringBuilder.toString());
            final eu.domibus.core.alerts.model.persist.Event eventEntity = domainConverter.convert(event, eu.domibus.core.alerts.model.persist.Event.class);
            eventDao.create(eventEntity);
            return domainConverter.convert(eventEntity, Event.class);
        } catch (EbMS3Exception e) {
            LOG.error("Message:[{}] Errors while enriching message event", messageId, e);
            return null;
        }

    }*/
}

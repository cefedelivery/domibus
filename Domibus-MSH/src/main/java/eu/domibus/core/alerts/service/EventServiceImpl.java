package eu.domibus.core.alerts.service;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.AuthenticationEvent;
import eu.domibus.core.alerts.model.common.CertificateEvent;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
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

import java.util.Date;

import static eu.domibus.core.alerts.model.common.AuthenticationEvent.LOGIN_TIME;
import static eu.domibus.core.alerts.model.common.AuthenticationEvent.USER;
import static eu.domibus.core.alerts.model.common.MessageEvent.*;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class EventServiceImpl implements EventService {

    private final static Logger LOG = LoggerFactory.getLogger(EventServiceImpl.class);
    public static final String MESSAGE_EVENT_SELECTOR = "message";
    public static final String LOGIN_FAILURE = "loginFailure";
    public static final String ACCOUNT_DISABLED = "accountDisabled";
    public static final String CERTIFICATE_EXPIRED = "certificateExpired";
    public static final String CERTIFICATE_IMMINENT_EXPIRATION = "certificateImminentExpiration";

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueMessageEvent(
            final String messageId,
            final MessageStatus oldStatus,
            final MessageStatus newStatus,
            final MSHRole role) {
        //check is status is relevant.
        Event event = new Event(EventType.MSG_COMMUNICATION_FAILURE);
        event.addKeyValue(OLD_STATUS.name(), oldStatus.name());
        event.addKeyValue(NEW_STATUS.name(), newStatus.name());
        event.addKeyValue(MESSAGE_ID.name(), messageId);
        event.addKeyValue(ROLE.name(), role.name());
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, MESSAGE_EVENT_SELECTOR);
        LOG.debug("Event:[{}] added to the queue", event);
    }

    @Override
    public void enqueueLoginFailureEvent(
            final String userName,
            final Date loginTime,
            final boolean accountDisabled) {
        //check is status is relevant.
        Event event = preparetAuthenticatorEvent(userName, loginTime.toString(), Boolean.valueOf(accountDisabled).toString(),EventType.USER_LOGIN_FAILURE);
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, LOGIN_FAILURE);
        LOG.debug("Event:[{}] added to the queue", event);
    }

    @Override
    public void enqueueAccountDisabledEvent(
            final String userName,
            final Date loginTime,
            final boolean accountDisabled) {
        //check is status is relevant.
        Event event = preparetAuthenticatorEvent(userName, loginTime.toString(), Boolean.valueOf(accountDisabled).toString(),EventType.USER_ACCOUNT_DISABLED);
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, ACCOUNT_DISABLED);
        LOG.debug("Event:[{}] added to the queue", event);
    }

    @Override
    public void enqueueImminentCertificateExpirationEvent(final String accessPoint, final String alias, final Date expirationDate){
        EventType eventType=EventType.CERT_IMMINENT_EXPIRATION;
        final Event event = prepareCertificateEvent(accessPoint, alias, expirationDate, eventType);
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, CERTIFICATE_IMMINENT_EXPIRATION);
        LOG.debug("Event:[{}] added to the queue", event);
    }

    @Override
    public void enqueueCertificateExpiredEvent(final String accessPoint, final String alias, final Date expirationDate){
        EventType eventType=EventType.CERT_EXPIRED;
        final Event event = prepareCertificateEvent(accessPoint, alias, expirationDate, eventType);
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, CERTIFICATE_EXPIRED);
        LOG.debug("Event:[{}] added to the queue", event);
    }

    private Event prepareCertificateEvent(String accessPoint, String alias, Date expirationDate, EventType eventType) {
        Event event = new Event(eventType);
        event.addKeyValue(CertificateEvent.ACCESS_POINT.name(),accessPoint);
        event.addKeyValue(CertificateEvent.ALIAS.name(),alias);
        event.addKeyValue(CertificateEvent.EXPIRATION_DATE.name(),expirationDate.toString());
        return event;
    }

    private Event preparetAuthenticatorEvent(
            final String userName,
            final String loginTime,
            final String accountDisabled,
            final EventType eventType) {
        Event event = new Event(eventType);
        event.addKeyValue(USER.name(), userName);
        event.addKeyValue(LOGIN_TIME.name(), loginTime);
        event.addKeyValue(AuthenticationEvent.ACCOUNT_DISABLED.name(), accountDisabled);
        return event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistEvent(final Event event) {
        final eu.domibus.core.alerts.model.persist.Event eventEntity = domainConverter.convert(event, eu.domibus.core.alerts.model.persist.Event.class);
        LOG.debug("Converting jms event\n[{}] to persistent event\n[{}]", event, eventEntity);
        eventEntity.enrichProperties();
        eventDao.create(eventEntity);
        event.setEntityId(eventEntity.getEntityId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enrichMessageEvent(final Event event) {
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
}

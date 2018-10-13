package eu.domibus.core.alerts.service;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.security.User;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.AuthenticationEvent;
import eu.domibus.core.alerts.model.common.CertificateEvent;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.AlertEventModuleConfiguration;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static eu.domibus.core.alerts.model.common.AuthenticationEvent.LOGIN_TIME;
import static eu.domibus.core.alerts.model.common.AuthenticationEvent.USER;
import static eu.domibus.core.alerts.model.common.MessageEvent.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class EventServiceImpl implements EventService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(EventServiceImpl.class);

    static final String MESSAGE_EVENT_SELECTOR = "message";

    static final String LOGIN_FAILURE = "loginFailure";

    static final String ACCOUNT_DISABLED = "accountDisabled";

    static final String CERTIFICATE_EXPIRED = "certificateExpired";

    static final String CERTIFICATE_IMMINENT_EXPIRATION = "certificateImminentExpiration";

    private static final String EVENT_ADDED_TO_THE_QUEUE = "Event:[{}] added to the queue";

    private static final int MAX_DESCRIPTION_LENGTH = 255;

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

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueMessageEvent(
            final String messageId,
            final MessageStatus oldStatus,
            final MessageStatus newStatus,
            final MSHRole role) {
        Event event = new Event(EventType.MSG_STATUS_CHANGED);
        event.addStringKeyValue(OLD_STATUS.name(), oldStatus.name());
        event.addStringKeyValue(NEW_STATUS.name(), newStatus.name());
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        event.addStringKeyValue(ROLE.name(), role.name());
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, MESSAGE_EVENT_SELECTOR);
        LOG.debug(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueLoginFailureEvent(
            final String userName,
            final Date loginTime,
            final boolean accountDisabled) {
        Event event = prepareAuthenticatorEvent(userName, loginTime, Boolean.toString(accountDisabled), EventType.USER_LOGIN_FAILURE);
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, LOGIN_FAILURE);
        LOG.debug(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueAccountDisabledEvent(
            final String userName,
            final Date accountDisabledTime,
            final boolean accountDisabled) {
        Event event = prepareAuthenticatorEvent(userName, accountDisabledTime, Boolean.toString(accountDisabled), EventType.USER_ACCOUNT_DISABLED);
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, ACCOUNT_DISABLED);
        LOG.debug(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueImminentCertificateExpirationEvent(final String accessPoint, final String alias, final Date expirationDate) {
        EventType eventType = EventType.CERT_IMMINENT_EXPIRATION;
        final Event event = prepareCertificateEvent(accessPoint, alias, expirationDate, eventType);
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, CERTIFICATE_IMMINENT_EXPIRATION);
        LOG.debug(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueCertificateExpiredEvent(final String accessPoint, final String alias, final Date expirationDate) {
        EventType eventType = EventType.CERT_EXPIRED;
        final Event event = prepareCertificateEvent(accessPoint, alias, expirationDate, eventType);
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, CERTIFICATE_EXPIRED);
        LOG.debug(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public eu.domibus.core.alerts.model.persist.Event persistEvent(final Event event) {
        final eu.domibus.core.alerts.model.persist.Event eventEntity = domainConverter.convert(event, eu.domibus.core.alerts.model.persist.Event.class);
        LOG.debug("Converting jms event\n[{}] to persistent event\n[{}]", event, eventEntity);
        eventEntity.enrichProperties();
        eventDao.create(eventEntity);
        event.setEntityId(eventEntity.getEntityId());
        return eventEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enrichMessageEvent(final Event event) {
        final Optional<String> messageIdProperty = event.findStringProperty(MESSAGE_ID.name());
        final Optional<String> roleProperty = event.findStringProperty(ROLE.name());
        if (!messageIdProperty.isPresent() || !roleProperty.isPresent()) {
            LOG.error("Message id and role are mandatory for message event[{}].", event);
            throw new IllegalStateException("Message id and role are mandatory for message event.");
        }
        final String messageId = messageIdProperty.get();
        final String role = roleProperty.get();
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        final MessageExchangeConfiguration userMessageExchangeContext;
        try {
            userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.valueOf(role));
            final Party senderParty = pModeProvider.getSenderParty(userMessageExchangeContext.getPmodeKey());
            final Party receiverParty = pModeProvider.getReceiverParty(userMessageExchangeContext.getPmodeKey());
            event.addStringKeyValue(FROM_PARTY.name(), senderParty.getName());
            event.addStringKeyValue(TO_PARTY.name(), receiverParty.getName());
            StringBuilder errors = new StringBuilder();
            errorLogDao.
                    getErrorsForMessage(messageId).
                    stream().
                    map(ErrorLogEntry::getErrorDetail).forEach(errors::append);
            if (!errors.toString().isEmpty()) {
                event.addStringKeyValue(DESCRIPTION.name(), StringUtils.truncate(errors.toString(), MAX_DESCRIPTION_LENGTH));
            }
        } catch (EbMS3Exception e) {
            LOG.error("Message:[{}] Errors while enriching message event", messageId, e);
        }
    }

    private Event prepareCertificateEvent(String accessPoint, String alias, Date expirationDate, EventType eventType) {
        Event event = new Event(eventType);
        event.addStringKeyValue(CertificateEvent.ACCESS_POINT.name(), accessPoint);
        event.addStringKeyValue(CertificateEvent.ALIAS.name(), alias);
        event.addDateKeyValue(CertificateEvent.EXPIRATION_DATE.name(), expirationDate);
        return event;
    }

    private Event prepareAuthenticatorEvent(
            final String userName,
            final Date loginTime,
            final String accountDisabled,
            final EventType eventType) {
        Event event = new Event(eventType);
        event.addStringKeyValue(USER.name(), userName);
        event.addDateKeyValue(LOGIN_TIME.name(), loginTime);
        event.addStringKeyValue(AuthenticationEvent.ACCOUNT_DISABLED.name(), accountDisabled);
        return event;
    }

    @Override
    public void enqueuePasswordExpiredEvent(User user, Integer maxPasswordAgeInDays) {
        enqueuePasswordEvent(EventType.PASSWORD_EXPIRED, user, maxPasswordAgeInDays);
    }

    @Override
    public void enqueuePasswordImminentExpirationEvent(User user, Integer maxPasswordAgeInDays) {
        enqueuePasswordEvent(EventType.PASSWORD_IMMINENT_EXPIRATION, user, maxPasswordAgeInDays);
    }

    protected void enqueuePasswordEvent(EventType eventType, User user, Integer maxPasswordAgeInDays) {

        Event event = preparePasswordEvent(user, eventType, maxPasswordAgeInDays);
        eu.domibus.core.alerts.model.persist.Event entity = getPersistedEvent(event);

        if (!this.shouldCreateAlert(entity)) {
            return;
        }

        entity.setLastAlertDate(LocalDate.now());
        eventDao.update(entity);

        jmsManager.convertAndSendToQueue(event, alertMessageQueue, EventType.getQueueSelectorFromEventType(eventType));
        LOG.trace(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    private eu.domibus.core.alerts.model.persist.Event getPersistedEvent(Event event) {

        String sourceId = event.findStringProperty("SOURCE").get();
        eu.domibus.core.alerts.model.persist.Event entity = eventDao.findWithTypeAndPropertyValue(event.getType(), "SOURCE", sourceId);
        if (entity == null) {
            entity = this.persistEvent(event);
        }
        return entity;
    }

    private boolean shouldCreateAlert(eu.domibus.core.alerts.model.persist.Event entity) {

        AlertType alertType = AlertType.getAlertTypeFromEventType(entity.getType());
        final AlertEventModuleConfiguration eventConfiguration = multiDomainAlertConfigurationService.getRepetitiveEventConfiguration(alertType);
        if (!eventConfiguration.isActive()) {
            return false;
        }

        int frequency = eventConfiguration.getEventFrequency();

        LocalDate lastAlertDate = entity.getLastAlertDate();
        LocalDate notificationDate = LocalDate.now().minusDays(frequency);

        if (lastAlertDate == null) {
            return true;
        }
        if (lastAlertDate.isBefore(notificationDate)) {
            return true; // last alert is old enough to send another one
        }

        return false;
    }


    private Event preparePasswordEvent(User user, EventType eventType, Integer maxPasswordAgeInDays) {
        Event event = new Event(eventType);
        event.setReportingTime(new Date());
        event.addStringKeyValue("SOURCE", "User_" + user.getEntityId());
        event.addStringKeyValue("USER", user.getUserName());

        LocalDate expDate = user.getPasswordChangeDate().plusDays(maxPasswordAgeInDays);
        Date date = Date.from(expDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        event.addDateKeyValue("EXPIRATION_DATE", date);

        return event;
    }

}

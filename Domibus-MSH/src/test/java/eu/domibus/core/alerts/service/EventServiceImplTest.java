package eu.domibus.core.alerts.service;

import com.google.common.collect.Lists;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.persist.AbstractEventProperty;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static eu.domibus.core.alerts.model.common.AuthenticationEvent.*;
import static eu.domibus.core.alerts.model.common.CertificateEvent.*;
import static eu.domibus.core.alerts.model.common.MessageEvent.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class EventServiceImplTest {

    @Tested
    private EventServiceImpl eventService;

    @Injectable
    private EventDao eventDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private ErrorLogDao errorLogDao;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private Queue alertMessageQueue;


    @Test
    public void enqueueMessageEvent() {
        final String messageId = "messageId";
        final MessageStatus oldMessageStatus = MessageStatus.SEND_ENQUEUED;
        final MessageStatus newMessageStatus = MessageStatus.ACKNOWLEDGED;
        final MSHRole mshRole = MSHRole.SENDING;
        eventService.enqueueMessageEvent(messageId, oldMessageStatus, newMessageStatus, mshRole);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, eventService.MESSAGE_EVENT_SELECTOR);
            times = 1;
            Assert.assertEquals(oldMessageStatus.name(), event.getProperties().get(OLD_STATUS.name()).getValue());
            Assert.assertEquals(newMessageStatus.name(), event.getProperties().get(NEW_STATUS.name()).getValue());
            Assert.assertEquals(messageId, event.getProperties().get(MESSAGE_ID.name()).getValue());
            Assert.assertEquals(mshRole.name(), event.getProperties().get(ROLE.name()).getValue());
        }};
    }

    @Test
    public void enqueueLoginFailureEvent() throws ParseException {
        final String userName = "thomas";
        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        final Date loginTime = parser.parse("25/10/1977 00:00:00");
        final boolean accountDisabled = false;
        eventService.enqueueLoginFailureEvent(userName, loginTime, accountDisabled);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, eventService.LOGIN_FAILURE);
            times = 1;
            Assert.assertEquals(userName, event.getProperties().get(USER.name()).getValue());
            Assert.assertEquals(loginTime.toString(), event.getProperties().get(LOGIN_TIME.name()).getValue());
            Assert.assertEquals("false", event.getProperties().get(ACCOUNT_DISABLED.name()).getValue());
        }};

    }

    @Test
    public void enqueueAccountDisabledEvent() throws ParseException {
        final String userName = "thomas";
        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        final Date loginTime = parser.parse("25/10/1977 00:00:00");
        final boolean accountDisabled = false;
        eventService.enqueueAccountDisabledEvent(userName, loginTime, accountDisabled);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, eventService.ACCOUNT_DISABLED);
            times = 1;
            Assert.assertEquals(userName, event.getProperties().get(USER.name()).getValue());
            Assert.assertEquals(loginTime.toString(), event.getProperties().get(LOGIN_TIME.name()).getValue());
            Assert.assertEquals("false", event.getProperties().get(ACCOUNT_DISABLED.name()).getValue());
        }};
    }

    @Test
    public void enqueueImminentCertificateExpirationEvent() throws ParseException {
        final String accessPoint = "red_gw";
        final String alias = "blue_gw";
        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        final Date expirationDate = parser.parse("25/10/1977 00:00:00");
        eventService.enqueueImminentCertificateExpirationEvent(accessPoint, alias, expirationDate);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, eventService.CERTIFICATE_IMMINENT_EXPIRATION);
            times = 1;
            Assert.assertEquals(accessPoint, event.getProperties().get(ACCESS_POINT.name()).getValue());
            Assert.assertEquals(alias, event.getProperties().get(ALIAS.name()).getValue());
            Assert.assertEquals(expirationDate.toString(), event.getProperties().get(EXPIRATION_DATE.name()).getValue());
        }};
    }

    @Test
    public void enqueueCertificateExpiredEvent() throws ParseException {
        final String accessPoint = "red_gw";
        final String alias = "blue_gw";
        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        final Date expirationDate = parser.parse("25/10/1977 00:00:00");
        eventService.enqueueCertificateExpiredEvent(accessPoint, alias, expirationDate);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, eventService.CERTIFICATE_EXPIRED);
            times = 1;
            Assert.assertEquals(accessPoint, event.getProperties().get(ACCESS_POINT.name()).getValue());
            Assert.assertEquals(alias, event.getProperties().get(ALIAS.name()).getValue());
            Assert.assertEquals(expirationDate.toString(), event.getProperties().get(EXPIRATION_DATE.name()).getValue());
        }};
    }

    @Test
    public void persistEvent() {
        Event event = new Event();

        eu.domibus.core.alerts.model.persist.Event persistedEvent = new eu.domibus.core.alerts.model.persist.Event();
        persistedEvent.setEntityId(1);
        final StringEventProperty stringEventProperty = new StringEventProperty();
        stringEventProperty.setStringValue("value");
        final String key = "key";
        persistedEvent.getProperties().put(key, stringEventProperty);

        new Expectations() {{
            domainConverter.convert(event, eu.domibus.core.alerts.model.persist.Event.class);
            result = persistedEvent;

        }};
        eventService.persistEvent(event);
        new Verifications() {{
            eu.domibus.core.alerts.model.persist.Event capture;
            eventDao.create(capture = withCapture());
            final AbstractEventProperty stringEventProperty1 = capture.getProperties().get(key);
            Assert.assertEquals(key, stringEventProperty1.getKey());
            Assert.assertEquals(persistedEvent, stringEventProperty1.getEvent());
            Assert.assertEquals(1, event.getEntityId());

        }};
    }

    @Test
    public void enrichMessageEvent(@Mocked final UserMessage userMessage,
                                   @Mocked final MessageExchangeConfiguration userMessageExchangeContext) throws EbMS3Exception {
        final Event event = new Event();
        final String messageId = "messageId";
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        event.addStringKeyValue(ROLE.name(), "SENDING");
        final ErrorLogEntry errorLogEntry=new ErrorLogEntry();
        final String error_detail = "Error detail";
        errorLogEntry.setErrorDetail(error_detail);
        final String fromParty = "blue_gw";
        final String toParty = "red_gw";

        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result=userMessageExchangeContext;

            userMessageExchangeContext.getPmodeKey();
            result="pmodekey";

            pModeProvider.getSenderParty(userMessageExchangeContext.getPmodeKey()).getName();

            result= fromParty;

            pModeProvider.getReceiverParty(userMessageExchangeContext.getPmodeKey()).getName();

            result= toParty;

            errorLogDao.
                    getErrorsForMessage(messageId);
            result= Lists.newArrayList(errorLogEntry);
        }};
        eventService.enrichMessageEvent(event);
        Assert.assertEquals(fromParty,event.getProperties().get(FROM_PARTY.name()).getValue());
        Assert.assertEquals(toParty,event.getProperties().get(TO_PARTY.name()).getValue());
        Assert.assertEquals(error_detail,event.getProperties().get(DESCRIPTION.name()).getValue());
    }
    @Test(expected = IllegalArgumentException.class)
    public void enrichMessageEventWithIllegalArgumentExcption(@Mocked final UserMessage userMessage,
                                   @Mocked final MessageExchangeConfiguration userMessageExchangeContext) throws EbMS3Exception {
        final Event event = new Event();
        final String messageId = "messageId";
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        eventService.enrichMessageEvent(event);
    }
}
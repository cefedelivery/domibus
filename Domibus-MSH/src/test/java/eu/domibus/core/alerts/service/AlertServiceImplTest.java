package eu.domibus.core.alerts.service;

import com.google.common.collect.Lists;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.*;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.converter.DomainCoreConverter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static eu.domibus.core.alerts.model.common.MessageEvent.MESSAGE_ID;
import static eu.domibus.core.alerts.model.common.MessageEvent.OLD_STATUS;
import static eu.domibus.core.alerts.service.AlertServiceImpl.*;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class AlertServiceImplTest {


    @Tested
    AlertServiceImpl alertService;

    @Injectable
    private EventDao eventDao;

    @Injectable
    private AlertDao alertDao;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private Queue alertMessageQueue;

    @Injectable
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Test
    public void createAlertOnEvent() {
        final Event event = new Event();
        event.setEntityId(1);
        event.setType(EventType.MSG_COMMUNICATION_FAILURE);

        final eu.domibus.core.alerts.model.persist.Event eventEntity = new eu.domibus.core.alerts.model.persist.Event();
        new Expectations() {{
            eventDao.read(event.getEntityId());
            result = eventEntity;

            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, "1");
            result = 5;

            multiDomainAlertConfigurationService.getAlertLevel(withAny(new Alert()));
            result = AlertLevel.HIGH;
        }};
        alertService.createAlertOnEvent(event);
        new VerificationsInOrder() {{
            eu.domibus.core.alerts.model.persist.Alert alert;
            alertDao.create(alert = withCapture());
            times = 1;
            assertEquals(AlertType.MSG_COMMUNICATION_FAILURE, alert.getAlertType());
            assertEquals(0, alert.getAttempts(), 0);
            assertEquals(5, alert.getMaxAttempts(), 0);
            assertEquals(AlertStatus.SEND_ENQUEUED, alert.getAlertStatus());
            assertNotNull(alert.getCreationTime());
            assertNull(alert.getReportingTime());
            assertEquals(AlertLevel.HIGH, alert.getAlertLevel());
            assertTrue(alert.getEvents().contains(eventEntity));
            domainConverter.convert(alert, eu.domibus.core.alerts.model.service.Alert.class);
            times = 1;
        }};
    }

    @Test
    public void enqueueAlert() {
        Alert alert = new Alert();
        alertService.enqueueAlert(alert);
        new Verifications() {{
            jmsManager.convertAndSendToQueue(alert, alertMessageQueue, ALERT_SELECTOR);
        }};
    }

    @Test
    public void getMailModelForAlert() throws ParseException {
        final String mailSubjet = "Message failure";
        final String messageId = "messageId";
        final int entityId = 1;
        final AlertType alertType = AlertType.MSG_COMMUNICATION_FAILURE;
        final AlertLevel alertLevel = AlertLevel.HIGH;

        Alert alert = new Alert();
        alert.setEntityId(entityId);

        final eu.domibus.core.alerts.model.persist.Alert persistedAlert = new eu.domibus.core.alerts.model.persist.Alert();
        persistedAlert.setAlertType(alertType);
        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        Date reportingTime = parser.parse("25/10/1977 00:00:00");
        persistedAlert.setAlertLevel(alertLevel);

        final eu.domibus.core.alerts.model.persist.Event event = new eu.domibus.core.alerts.model.persist.Event();
        persistedAlert.addEvent(event);

        final StringEventProperty messageIdProperty = new StringEventProperty();
        messageIdProperty.setStringValue(messageId);
        event.addProperty(MESSAGE_ID.name(), messageIdProperty);

        final StringEventProperty oldStatusProperty = new StringEventProperty();
        oldStatusProperty.setStringValue(MessageStatus.SEND_ENQUEUED.name());
        event.addProperty(OLD_STATUS.name(), oldStatusProperty);
        new Expectations() {{
            alertDao.read(entityId);
            result = persistedAlert;
            multiDomainAlertConfigurationService.getMailSubject(alertType);
            result = mailSubjet;
        }};
        final MailModel mailModelForAlert = alertService.getMailModelForAlert(alert);
        assertEquals(mailSubjet, mailModelForAlert.getSubject());
        assertEquals(alertType.getTemplate(), mailModelForAlert.getTemplatePath());
        final Map<String, String> model = (Map<String, String>) mailModelForAlert.getModel();
        assertEquals(messageId, model.get(MESSAGE_ID.name()));
        assertEquals(MessageStatus.SEND_ENQUEUED.name(), model.get(OLD_STATUS.name()));
        assertEquals(alertLevel.name(), model.get(ALERT_LEVEL));
        assertNotNull(reportingTime.toString(), model.get(REPORTING_TIME));

    }

    @Test
    public void handleAlertStatusSuccess(final @Mocked eu.domibus.core.alerts.model.persist.Alert persistedAlert) {
        final Alert alert = new Alert();
        final int entityId = 1;
        alert.setEntityId(entityId);
        alert.setAlertStatus(AlertStatus.SUCCESS);
        new Expectations() {{
            alertDao.read(entityId);times=1;
            result = persistedAlert;
            persistedAlert.getAlertStatus();
            result=AlertStatus.SUCCESS;
        }};
        alertService.handleAlertStatus(alert);
        new VerificationsInOrder() {{
            persistedAlert.setAlertStatus(AlertStatus.SUCCESS);times=1;
            persistedAlert.setNextAttempt(null);times=1;
            persistedAlert.setReportingTime(withAny(new Date()));times=1;
        }};
    }

    @Test
    public void handleAlertStatusFailedWithRemainingAttempts(final @Mocked eu.domibus.core.alerts.model.persist.Alert persistedAlert, @Mocked final org.joda.time.LocalDateTime dateTime) throws ParseException {
        final int nextAttemptInMinutes = 10;
        final Alert alert = new Alert();
        final int entityId = 1;
        alert.setEntityId(entityId);
        alert.setAlertStatus(AlertStatus.FAILED);

        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        Date nextAttempt = parser.parse("25/10/1977 00:00:00");

        new Expectations() {{
            alertDao.read(entityId);times=1;
            result = persistedAlert;

            persistedAlert.getAlertStatus();
            result=AlertStatus.FAILED;
            persistedAlert.getAttempts();
            result=0;
            persistedAlert.getMaxAttempts();
            result=2;

            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RETRY_TIME);
            result = nextAttemptInMinutes;

            dateTime.now().plusMinutes(nextAttemptInMinutes).toDate();
            result=nextAttempt;

        }};
        alertService.handleAlertStatus(alert);
        new VerificationsInOrder() {{
            persistedAlert.setAlertStatus(AlertStatus.FAILED);times=1;
            persistedAlert.setNextAttempt(nextAttempt);
            persistedAlert.setAttempts(1);times=1;
            persistedAlert.setAlertStatus(AlertStatus.RETRY);times=1;

        }};
    }

    @Test
    public void handleAlertStatusFailedWithNoMoreAttempts(final @Mocked eu.domibus.core.alerts.model.persist.Alert persistedAlert, @Mocked final org.joda.time.LocalDateTime dateTime) throws ParseException {
        final Alert alert = new Alert();
        final int entityId = 1;
        alert.setEntityId(entityId);
        alert.setAlertStatus(AlertStatus.FAILED);

        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        Date failureTime = parser.parse("25/10/1977 00:00:00");

        new Expectations() {{
            alertDao.read(entityId);times=1;
            result = persistedAlert;

            persistedAlert.getAlertStatus();
            result=AlertStatus.FAILED;

            dateTime.now().toDate();
            result=failureTime;
        }};
        alertService.handleAlertStatus(alert);
        new VerificationsInOrder() {{
            persistedAlert.setAlertStatus(AlertStatus.FAILED);times=1;
            persistedAlert.setNextAttempt(null);

            persistedAlert.setAttempts(withAny(0));times=0;
            persistedAlert.setReportingTimeFailure(failureTime);times=1;
        }};
    }


    @Test
    public void retrieveAndResendFailedAlerts() {
        eu.domibus.core.alerts.model.persist.Alert firtRetryAlert = new eu.domibus.core.alerts.model.persist.Alert();
        firtRetryAlert.setEntityId(1);
        eu.domibus.core.alerts.model.persist.Alert secondRetryAlert = new eu.domibus.core.alerts.model.persist.Alert();
        firtRetryAlert.setEntityId(2);
        final Alert firtsConvertedAlert = new Alert();
        final Alert secondConvertedAlert = new Alert();
        final List<eu.domibus.core.alerts.model.persist.Alert> alerts = Lists.newArrayList(firtRetryAlert);
        alerts.add(secondRetryAlert);
        new Expectations() {{
            alertDao.findRetryAlerts();
            result = alerts;
            domainConverter.convert(firtRetryAlert, eu.domibus.core.alerts.model.service.Alert.class);
            result = firtsConvertedAlert;
            domainConverter.convert(secondRetryAlert, eu.domibus.core.alerts.model.service.Alert.class);
            result = secondConvertedAlert;
        }};
        alertService.retrieveAndResendFailedAlerts();
        new Verifications() {{
            jmsManager.convertAndSendToQueue(withAny(new Alert()), alertMessageQueue, ALERT_SELECTOR);
            times = 2;

        }};
    }

    @Test
    public void findAlerts() {
        final AlertCriteria alertCriteria = new AlertCriteria();
        final ArrayList<eu.domibus.core.alerts.model.persist.Alert> alerts = Lists.newArrayList(new eu.domibus.core.alerts.model.persist.Alert());
        new Expectations(){{
            alertDao.filterAlerts(alertCriteria);
            result = alerts;
        }};
        alertService.findAlerts(alertCriteria);
        new Verifications(){{
           domainConverter.convert(alerts,eu.domibus.core.alerts.model.service.Alert.class);times=1;
        }};

    }

    @Test
    public void countAlerts() {
        final AlertCriteria alertCriteria = new AlertCriteria();
        alertService.countAlerts(alertCriteria);
        new Verifications() {{
            alertDao.countAlerts(alertCriteria);
            times = 1;
        }};
    }

    @Test
    public void cleanAlerts(final @Mocked org.joda.time.LocalDateTime localDateTime) throws ParseException {
        final int alertLifeTimeInDays = 10;
        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        Date alertLimitDate = parser.parse("25/10/1977 00:00:00");
        final List<eu.domibus.core.alerts.model.persist.Alert> alerts = Lists.newArrayList(new eu.domibus.core.alerts.model.persist.Alert());
        new Expectations() {{
            multiDomainAlertConfigurationService.getCommonConfiguration().getAlertLifeTimeInDays();
            result = alertLifeTimeInDays;
            localDateTime.now().minusDays(alertLifeTimeInDays).withTime(0, 0, 0, 0).toDate();
            result = alertLimitDate;
            alertDao.retrieveAlertsWithCreationDateSmallerThen(alertLimitDate);
            result = alerts;

        }};
        alertService.cleanAlerts();
        new Verifications() {{
            alertDao.deleteAll(alerts);
            times = 1;
        }};
    }

    @Test
    public void updateAlertProcessed() {
        final Alert firstAlert = new Alert();
        final int firstEntityId = 1;
        firstAlert.setEntityId(firstEntityId);
        firstAlert.setProcessed(false);
        List<Alert> alerts = Lists.newArrayList(firstAlert);
        final Alert secondAlert = new Alert();
        secondAlert.setProcessed(true);
        final int secondEntityId = 2;
        secondAlert.setEntityId(secondEntityId);
        alerts.add(secondAlert);

        alertService.updateAlertProcessed(alerts);

        new Verifications() {{
            List<Integer> entityIds = new ArrayList<>();
            List<Boolean> processeds = new ArrayList<>();
            alertDao.updateAlertProcessed(withCapture(entityIds), withCapture(processeds));
            assertEquals(firstEntityId, entityIds.get(0), 0);
            assertEquals(secondEntityId, entityIds.get(1), 0);
            assertEquals(false, processeds.get(0));
            assertEquals(true, processeds.get(1));
        }};
    }
}
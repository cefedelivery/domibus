package eu.domibus.core.alerts.service;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.*;
import eu.domibus.core.alerts.model.persist.EventProperty;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.converter.DomainCoreConverter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jms.Queue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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


    @Tested AlertServiceImpl alertService;

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

        final eu.domibus.core.alerts.model.persist.Event eventEntity=new eu.domibus.core.alerts.model.persist.Event();
        new Expectations(){{
            eventDao.read(event.getEntityId());
            result=eventEntity;

            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, "1");
            result=5;

            multiDomainAlertConfigurationService.getAlertLevel(withAny(new Alert()));
            result= AlertLevel.HIGH;
        }};
        alertService.createAlertOnEvent(event);
        new VerificationsInOrder(){{
            eu.domibus.core.alerts.model.persist.Alert alert;
            alertDao.create(alert=withCapture());times=1;
            assertEquals(AlertType.MSG_COMMUNICATION_FAILURE,alert.getAlertType());
            assertEquals(0,alert.getAttempts(),0);
            assertEquals(5,alert.getMaxAttempts(),0);
            assertEquals(AlertStatus.SEND_ENQUEUED,alert.getAlertStatus());
            assertNotNull(alert.getCreationTime());
            assertNull(alert.getReportingTime());
            assertEquals(AlertLevel.HIGH,alert.getAlertLevel());
            assertTrue(alert.getEvents().contains(eventEntity));
            domainConverter.convert(alert, eu.domibus.core.alerts.model.service.Alert.class);times=1;
        }};
    }

    @Test
    public void enqueueAlert() {
        Alert alert=new Alert();
        alertService.enqueueAlert(alert);
        new Verifications(){{
           jmsManager.convertAndSendToQueue(alert,alertMessageQueue,ALERT_SELECTOR);
        }};
    }

    @Test
    public void getMailModelForAlert() throws ParseException {
        final String mailSubjet = "Message failure";
        final String messageId = "messageId";
        final int entityId = 1;
        final AlertType alertType = AlertType.MSG_COMMUNICATION_FAILURE;
        final AlertLevel alertLevel = AlertLevel.HIGH;

        Alert alert=new Alert();
        alert.setEntityId(entityId);

        final eu.domibus.core.alerts.model.persist.Alert persistedAlert=new eu.domibus.core.alerts.model.persist.Alert();
        persistedAlert.setAlertType(alertType);
        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        Date reportingTime= parser.parse("25/10/1977 00:00:00");
        persistedAlert.setReportingTime(reportingTime);

        persistedAlert.setAlertLevel(alertLevel);

        final eu.domibus.core.alerts.model.persist.Event event = new eu.domibus.core.alerts.model.persist.Event();
        persistedAlert.addEvent(event);

        final EventProperty messageIdProperty = new EventProperty();
        messageIdProperty.setValue(messageId);
        event.addProperty(MESSAGE_ID.name(), messageIdProperty);

        final EventProperty oldStatusProperty = new EventProperty();
        oldStatusProperty.setValue(MessageStatus.SEND_ENQUEUED.name());
        event.addProperty(OLD_STATUS.name(), oldStatusProperty);
        new Expectations(){{
            alertDao.read(entityId);
            result=persistedAlert;
            multiDomainAlertConfigurationService.getMailSubject(alertType);
            result= mailSubjet;
        }};
        final MailModel mailModelForAlert = alertService.getMailModelForAlert(alert);
        assertEquals(mailSubjet,mailModelForAlert.getSubject());
        assertEquals(alertType.getTemplate(),mailModelForAlert.getTemplatePath());
        final Map<String,String> model = (Map<String, String>) mailModelForAlert.getModel();
        assertEquals(messageId, model.get(MESSAGE_ID.name()));
        assertEquals(MessageStatus.SEND_ENQUEUED.name(), model.get(OLD_STATUS.name()));
        assertEquals(alertLevel.name(), model.get(ALERT_LEVEL));
        assertEquals(reportingTime.toString(),model.get(REPORTING_TIME));

    }

    @Test
    public void handleAlertStatus() {
    }

    @Test
    public void retry() {
    }

    @Test
    public void findAlerts() {
    }

    @Test
    public void countAlerts() {
    }

    @Test
    public void cleanAlerts() {
    }

    @Test
    public void updateAlertProcessed() {
    }
}
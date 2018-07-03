package eu.domibus.core.alerts.dao;

import eu.domibus.audit.InMemoryDataBaseConfig;
import eu.domibus.audit.OracleDataBaseConfig;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.persist.EventProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class,
        OracleDataBaseConfig.class, AlertDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class AlertDaoTest {

    private final static Logger LOG = LoggerFactory.getLogger(AlertDaoTest.class);

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private EventDao eventDao;

    @Before
    public void setUp(){
        createAlert("blue_gw","red_gw",false,null);
        createAlert("blue_gw","red_gw",true,null);
    }


    public void createAlert(String fromParty,String toParty,boolean processed,Date reportingTime){
        Event event=new Event();
        final EventProperty blue_gw = new EventProperty();
        blue_gw.setValue(fromParty);

        final EventProperty red_gw = new EventProperty();
        red_gw.setValue(toParty);

        final EventProperty role = new EventProperty();
        role.setValue("SENDER");

        event.addProperty("FROM_PARTY", blue_gw);
        event.addProperty("TO_PARTY", red_gw);
        event.addProperty("ROLE", role);
        event.setType(EventType.MSG_COMMUNICATION_FAILURE);
        event.setReportingTime(new Date());


        Alert alert=new Alert();
        alert.setAlertStatus(AlertStatus.FAILED);
        alert.setAlertType(AlertType.MSG_COMMUNICATION_FAILURE);
        alert.addEvent(event);
        alert.setAlertLevel(AlertLevel.MEDIUM);
        alert.setMaxAttempts(1);
        alert.setCreationTime(new Date());
        alert.setProcessed(processed);
        alert.setReportingTime(reportingTime);


        alertDao.create(alert);
    }
    @Test
    public void findRetryAlertsOnParty() {

        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","blue_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        assertEquals(2,alerts.size());
        alerts.forEach(alert1 -> alert1.getEvents().
                forEach(event1 -> event1.getProperties().
                        forEach((ke, eventProperty) ->  LOG.info("Key[{}] value[{}]",ke,eventProperty.getValue()))));
    }

    @Test
    public void findRetryAlertsOnPartyButProcessed() {
        createAlert("black_gw","red_gw",true,null);
        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","black_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setProcessed(true);
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        assertEquals(1,alerts.size());
        alerts.forEach(alert1 -> alert1.getEvents().
                forEach(event1 -> event1.getProperties().
                        forEach((ke, eventProperty) ->  LOG.info("Key[{}] value[{}]",ke,eventProperty.getValue()))));
    }

    @Test
    public void findRetryAlertsOnPartyAndReportingTime() {
        final org.joda.time.LocalDateTime now = org.joda.time.LocalDateTime.now();
        final Date reportingDate = now.minusMinutes(15).toDate();
        final Date reportingFrom = now.minusMinutes(16).toDate();
        final Date reportingTo = now.minusMinutes(14).toDate();
        createAlert("blue_gw","red_gw",true,reportingDate);
        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","blue_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setReportingFrom(reportingFrom);
        alertCriteria.setReportingTo(reportingTo);
        alertCriteria.setProcessed(true);
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        assertEquals(1,alerts.size());
    }
}
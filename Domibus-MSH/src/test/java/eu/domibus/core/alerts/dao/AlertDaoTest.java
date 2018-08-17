package eu.domibus.core.alerts.dao;

import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.dao.InMemoryDataBaseConfig;
import eu.domibus.dao.OracleDataBaseConfig;
import eu.domibus.core.alerts.model.common.*;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class,
        OracleDataBaseConfig.class, AlertDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class AlertDaoTest {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(AlertDaoTest.class);

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
        final StringEventProperty blue_gw = new StringEventProperty();
        blue_gw.setStringValue(fromParty);

        final StringEventProperty red_gw = new StringEventProperty();
        red_gw.setStringValue(toParty);

        final StringEventProperty role = new StringEventProperty();
        role.setStringValue("SENDER");

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
    @Transactional
    public void findRetryAlertsOnParty() {

        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","blue_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setPage(0);
        alertCriteria.setPageSize(10);
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        assertEquals(2,alerts.size());
        alerts.forEach(alert1 -> alert1.getEvents().
                forEach(event1 -> event1.getProperties().
                        forEach((ke, eventProperty) ->  LOG.info("Key[{}] value[{}]",ke,eventProperty.getValue()))));
    }

    @Test
    @Transactional
    public void findRetryAlertsOnPartyButProcessed() {
        createAlert("black_gw","red_gw",true,null);
        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","black_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setProcessed(true);
        alertCriteria.setPage(0);
        alertCriteria.setPageSize(10);
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
        alertCriteria.setPage(0);
        alertCriteria.setPageSize(10);
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        assertEquals(1,alerts.size());
    }

    @Test
    public void countAlerts() {
        final org.joda.time.LocalDateTime now = org.joda.time.LocalDateTime.now();
        final Date reportingDate = now.minusMinutes(25).toDate();
        final Date reportingFrom = now.minusMinutes(26).toDate();
        final Date reportingTo = now.minusMinutes(24).toDate();
        createAlert("blue_gw","red_gw",true,reportingDate);
        final AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.getParameters().put("FROM_PARTY","blue_gw");
        alertCriteria.getParameters().put("TO_PARTY","red_gw");
        alertCriteria.setReportingFrom(reportingFrom);
        alertCriteria.setReportingTo(reportingTo);
        alertCriteria.setProcessed(true);
        final Long count = alertDao.countAlerts(alertCriteria);
        assertEquals(1,count.intValue());
    }
}
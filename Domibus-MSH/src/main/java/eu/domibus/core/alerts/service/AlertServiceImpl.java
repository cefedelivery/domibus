package eu.domibus.core.alerts.service;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.service.DefaultMailModel;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.converter.DomainCoreConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.core.alerts.model.common.AlertStatus.*;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class AlertServiceImpl implements AlertService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertServiceImpl.class);

    static final String DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS = "domibus.alert.retry.max_attempts";

    static final String ALERT_LEVEL = "ALERT_LEVEL";

    static final String REPORTING_TIME = "REPORTING_TIME";

    static final String ALERT_SELECTOR = "alert";

    static final String DOMIBUS_ALERT_RETRY_TIME = "domibus.alert.retry.time";

    @Autowired
    private EventDao eventDao;

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

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
    @Transactional
    public eu.domibus.core.alerts.model.service.Alert createAlertOnEvent(eu.domibus.core.alerts.model.service.Event event) {
        final Event eventEntity = eventDao.read(event.getEntityId());
        Alert alert = new Alert();
        alert.addEvent(eventEntity);
        alert.setAlertType(AlertType.getAlertTypeFromEventType(event.getType()));
        alert.setAttempts(0);
        alert.setMaxAttempts(Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, "1")));
        alert.setAlertStatus(SEND_ENQUEUED);
        alert.setCreationTime(new Date());

        final eu.domibus.core.alerts.model.service.Alert convertedAlert = domainConverter.convert(alert, eu.domibus.core.alerts.model.service.Alert.class);
        final AlertLevel alertLevel = multiDomainAlertConfigurationService.getAlertLevel(convertedAlert);
        alert.setAlertLevel(alertLevel);
        LOG.debug("Saving new alert:\n[{}]\n", alert);
        alertDao.create(alert);
        return domainConverter.convert(alert, eu.domibus.core.alerts.model.service.Alert.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueAlert(eu.domibus.core.alerts.model.service.Alert alert) {
        jmsManager.convertAndSendToQueue(alert, alertMessageQueue, ALERT_SELECTOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MailModel getMailModelForAlert(eu.domibus.core.alerts.model.service.Alert alert) {
        final Alert read = alertDao.read(alert.getEntityId());
        read.setReportingTime(new Date());
        Map<String, String> mailModel = new HashMap<>();
        final Event next = read.getEvents().iterator().next();
        next.getProperties().forEach((key, value) -> mailModel.put(key, value.getValue().toString()));
        mailModel.put(ALERT_LEVEL, read.getAlertLevel().name());
        mailModel.put(REPORTING_TIME, read.getReportingTime().toString());
        if (LOG.isDebugEnabled()) {
            mailModel.forEach((key,value)-> LOG.debug("Mail template key[{}] value[{}]", key, value));
        }
        final AlertType alertType = read.getAlertType();
        final String subject = multiDomainAlertConfigurationService.getMailSubject(alertType);
        final String template = alertType.getTemplate();
        return new DefaultMailModel(mailModel, template, subject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAlertStatus(eu.domibus.core.alerts.model.service.Alert alert) {
        final Alert alertEntity = alertDao.read(alert.getEntityId());
        alertEntity.setAlertStatus(alert.getAlertStatus());
        alertEntity.setNextAttempt(null);
        if (SUCCESS == alertEntity.getAlertStatus()) {
            alertEntity.setReportingTime(new Date());
            LOG.debug("Alert[{}]: send successfully",alert.getEntityId());
            return;
        }
        final Integer attempts = alertEntity.getAttempts() + 1;
        final Integer maxAttempts = alertEntity.getMaxAttempts();
        LOG.debug("Alert[{}]: send unsuccessfully",alert.getEntityId());
        if (attempts < maxAttempts) {
            LOG.debug("Alert[{}]: send attempts[{}], max attempts[{}]",alert.getEntityId(),attempts,maxAttempts);
            final Integer minutesBetweenAttempt = Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RETRY_TIME));
            final Date nextAttempt = org.joda.time.LocalDateTime.now().plusMinutes(minutesBetweenAttempt).toDate();
            alertEntity.setNextAttempt(nextAttempt);
            alertEntity.setAttempts(attempts);
            alertEntity.setAlertStatus(RETRY);
        }
        LOG.debug("Alert[{}]: change status to:[{}]",alert.getEntityId(),alertEntity.getAlertStatus());
        alertEntity.setReportingTimeFailure(org.joda.time.LocalDateTime.now().toDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retrieveAndResendFailedAlerts() {
        final List<Alert> retryAlerts = alertDao.findRetryAlerts();
        retryAlerts.forEach(this::convertAndEnqueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<eu.domibus.core.alerts.model.service.Alert> findAlerts(AlertCriteria alertCriteria) {
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        if(LOG.isDebugEnabled()){
            LOG.debug("Find alerts:");
            alerts.forEach(alert -> {
                LOG.debug("Alert[{}]",alert);
                alert.getEvents().forEach(event -> {
                    LOG.debug("Event[{}]",event);
                    event.getProperties().
                            forEach((key, value) -> LOG.debug("Event property:[{}]->[{}]",key,value));
                });
            });

        }
        return domainConverter.convert(alerts, eu.domibus.core.alerts.model.service.Alert.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long countAlerts(AlertCriteria alertCriteria) {
        return alertDao.countAlerts(alertCriteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void cleanAlerts(){
        final Integer alertLifeTimeInDays = multiDomainAlertConfigurationService.getCommonConfiguration().getAlertLifeTimeInDays();
        final Date alertLimitDate = org.joda.time.LocalDateTime.now().minusDays(alertLifeTimeInDays).withTime(0,0,0,0).toDate();
        LOG.debug("Cleaning alerts with creation time < [{}]",alertLimitDate);
        final List<Alert> alerts = alertDao.retrieveAlertsWithCreationDateSmallerThen(alertLimitDate);
        alertDao.deleteAll(alerts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateAlertProcessed(List<eu.domibus.core.alerts.model.service.Alert> alerts){
        alerts.forEach(alert -> {
            final int entityId = alert.getEntityId();
            final boolean processed = alert.isProcessed();
            LOG.debug("Update alert with id[{}] set processed to[{}]",entityId,processed);
            alertDao.updateAlertProcessed(entityId, processed);
        });

    }

    private void convertAndEnqueue(Alert alert) {
        LOG.debug("Preparing alert\n[{}]\nfor retry", alert);
        final eu.domibus.core.alerts.model.service.Alert convert = domainConverter.convert(alert, eu.domibus.core.alerts.model.service.Alert.class);
        enqueueAlert(convert);
    }

}

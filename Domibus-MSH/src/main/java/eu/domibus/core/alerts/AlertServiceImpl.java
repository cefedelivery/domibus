package eu.domibus.core.alerts;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.AlertType;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertServiceImpl implements AlertService{

    private final static Logger LOG = LoggerFactory.getLogger(AlertServiceImpl.class);
    public static final String DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS = "domibus.alert.retry.max_attempts";

    @Autowired
    private EventDao eventDao;

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    @Transactional
    public void processEvent(eu.domibus.core.alerts.model.Event event) {
        final Event eventEntity = eventDao.read(event.getEntityId());
        Alert alert=new Alert();
        alert.addEvent(eventEntity);
        alert.setAlertType(AlertType.MESSAGING);
        alert.setAttempts(0);
        alert.setMaxAttempts(Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS,"1")));
        alertDao.create(alert);
    }
}

package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.service.Event;

public interface AlertLevelStrategy {

    AlertLevel getAlertLevel(Event event);

}

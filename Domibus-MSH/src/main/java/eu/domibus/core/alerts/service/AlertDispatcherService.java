package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.service.Alert;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AlertDispatcherService {

    void dispatch(Alert alert);

}

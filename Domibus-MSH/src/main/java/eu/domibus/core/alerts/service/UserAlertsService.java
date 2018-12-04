package eu.domibus.core.alerts.service;

import eu.domibus.common.model.security.IUser;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface UserAlertsService {
    void triggetPasswordExpirationEvents();
    void triggerLoginFailureEvent(IUser user);
}

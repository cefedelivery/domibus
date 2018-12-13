package eu.domibus.core.alerts.service;

import eu.domibus.common.model.security.UserBase;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface UserAlertsService {
    void triggerPasswordExpirationEvents();

    void triggerLoginFailureEvent(UserBase user);
}
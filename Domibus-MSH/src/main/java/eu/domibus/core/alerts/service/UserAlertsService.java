package eu.domibus.core.alerts.service;

import eu.domibus.api.user.UserBase;
import eu.domibus.common.model.security.UserLoginErrorReason;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface UserAlertsService {
    void triggerPasswordExpirationEvents();

    void triggerLoginEvents(String userName, UserLoginErrorReason userLoginErrorReason);

    void triggerDisabledEvent(UserBase user);

//    void triggerLoginFailureEvent(UserEntityBase user);

}

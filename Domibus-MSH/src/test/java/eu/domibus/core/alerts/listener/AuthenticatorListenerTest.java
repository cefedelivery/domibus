package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@RunWith(JMockit.class)
public class AuthenticatorListenerTest {

    @Injectable
    private EventService eventService;

    @Injectable
    private AlertService alertService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Tested
    private AuthenticatorListener authenticatorListener;

    @Test
    public void onLoginFailure(@Mocked final Event event, @Mocked final Alert alert) {
        authenticatorListener.onLoginFailure(event, null);
        new Verifications() {{
            domainContextProvider.setCurrentDomain(withAny(new Domain()));times=0;
            eventService.persistEvent(event);
            alertService.createAlertOnEvent(event);
            alertService.enqueueAlert(alert);
        }};
    }

    @Test
    public void onAccountDisabled(@Mocked final Event event, @Mocked final Alert alert) {
        final String domain = "domain";
        new Expectations() {{
            alertService.createAlertOnEvent(event);
            result = alert;
        }};
        authenticatorListener.onAccountDisabled(event, domain);
        new Verifications() {{
            domainContextProvider.setCurrentDomain(domain);
            times = 1;
            eventService.persistEvent(event);
            alertService.createAlertOnEvent(event);
            alertService.enqueueAlert(alert);
        }};
    }

}
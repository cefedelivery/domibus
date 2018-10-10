package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.service.AlertDispatcherService;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class AlertListenerTest {

    @Injectable
    private AlertDispatcherService alertDispatcherService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Tested
    private AlertListener alertListener;

    @Test
    public void onAlertWithDomain(@Mocked final Alert alert) {
        final String domain = "domain";
        alertListener.onAlert(alert, domain);
        new Verifications(){{
            domainContextProvider.setCurrentDomain(domain); times=1;
            alertDispatcherService.dispatch(alert);
        }};
    }

    @Test
    public void onAlertWithSuper(@Mocked final Alert alert) {
        final String domain = null;
        alertListener.onAlert(alert, domain);
        new Verifications(){{
            domainContextProvider.setCurrentDomain(withAny(new Domain())); times=0;
            alertDispatcherService.dispatch(alert);
        }};
    }
}
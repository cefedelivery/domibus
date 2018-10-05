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
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class CertificateListenerTest {

    @Tested
    private CertificateListener certificateListener;

    @Injectable
    private EventService eventService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private AlertService alertService;

    @Test
    public void onImminentRevocationCertificateEvent(@Mocked final Event event, @Mocked final Alert alert) {
        new Expectations(){{
            alertService.createAlertOnEvent(event);
            result=alert;
        }};
        certificateListener.onImminentRevocationCertificateEvent(event, null);new Verifications(){{
            domainContextProvider.setCurrentDomain(withAny(new Domain()));times=0;
            eventService.persistEvent(event);
            alertService.createAlertOnEvent(event);
            alertService.enqueueAlert(alert);
        }};


    }

    @Test
    public void onRevokedCertificateEvent(@Mocked final Event event, @Mocked final Alert alert) {
        final String domain = "domain";
        new Expectations(){{
            alertService.createAlertOnEvent(event);
            result=alert;
        }};
        certificateListener.onRevokedCertificateEvent(event, domain);
        new Verifications(){{
            domainContextProvider.setCurrentDomain(domain);times=1;
            eventService.persistEvent(event);
            alertService.createAlertOnEvent(event);
            alertService.enqueueAlert(alert);
        }};
    }
}
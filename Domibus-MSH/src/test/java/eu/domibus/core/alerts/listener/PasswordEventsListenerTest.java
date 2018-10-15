package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.model.security.User;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class PasswordEventsListenerTest {

    @Tested
    private PasswordEventsListener passwordEventsListener;

    @Injectable
    private AlertService alertService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private EventDao eventDao;

    @Test
    public void testImminentExpirationEvent() throws Exception {
        setExpectations();
        passwordEventsListener.onImminentExpirationEvent(new Event(), "default");
        setVerifications();
    }

    @Test
    public void testExpiredEvent() throws Exception {
        setExpectations();
        passwordEventsListener.onExpiredEvent(new Event(), "default");
        setVerifications();
    }

    void setExpectations() {
        new Expectations() {{
            eventDao.read(anyInt);
            result = new eu.domibus.core.alerts.model.persist.Event();
        }};
    }

    void setVerifications() {
        new VerificationsInOrder() {{
            alertService.enqueueAlert((Alert) any);
            times = 1;
        }};
    }
}

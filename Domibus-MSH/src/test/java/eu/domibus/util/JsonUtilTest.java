package eu.domibus.util;

import eu.domibus.core.alerts.model.common.AuthenticationEvent;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
public class JsonUtilTest {

    JsonUtilImpl jsonUtil = new JsonUtilImpl();

    @Test
    public void testJsonSerialization() throws Exception {
        Event event = new Event();
        event.setEntityId(1);
        event.setType(EventType.USER_LOGIN_FAILURE);
        event.setReportingTime(new Date());

        event.addDateKeyValue(AuthenticationEvent.LOGIN_TIME.name(), new Date());
        event.addStringKeyValue(AuthenticationEvent.USER.name(), "baciuco");
        event.addStringKeyValue(AuthenticationEvent.ACCOUNT_DISABLED.name(), "false");
        final String eventString = jsonUtil.writeValueAsString(event);

        Event event1 = jsonUtil.readValue(eventString, Event.class);
        Assert.assertEquals(event, event1);
    }
}

package eu.domibus.common.services.impl;

import eu.domibus.core.alerts.model.common.AuthenticationEvent;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import mockit.integration.junit4.JMockit;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class AlertBeanConversionTest {


    @Before
    public void setUp(){

    }
    @Test
    public void testConversion() throws ParseException {
        final String user = "user";
        final String accountDisabled = "false";

        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        final Date reportingTime = parser.parse("25/10/2001 00:00:00");
        final Date loginTime = parser.parse("26/10/2001 00:00:00");
        List myMappingFiles = new ArrayList();
        myMappingFiles.add("config/DomainCoreBeanMapping.xml");
        DozerBeanMapper mapper = new DozerBeanMapper();
        mapper.setMappingFiles(myMappingFiles);

        Event event=new Event();
        event.setEntityId(1);
        event.setType(EventType.USER_LOGIN_FAILURE);
        event.setReportingTime(reportingTime);

        event.addDateKeyValue(AuthenticationEvent.LOGIN_TIME.name(),loginTime);
        event.addStringKeyValue(AuthenticationEvent.USER.name(), user);
        event.addStringKeyValue(AuthenticationEvent.ACCOUNT_DISABLED.name(), accountDisabled);


        final eu.domibus.core.alerts.model.persist.Event persistEvent = mapper.map(event, eu.domibus.core.alerts.model.persist.Event.class);
        Assert.assertEquals(1,persistEvent.getEntityId());
        Assert.assertEquals(EventType.USER_LOGIN_FAILURE,persistEvent.getType());
        Assert.assertEquals(reportingTime,persistEvent.getReportingTime());

        Assert.assertEquals(loginTime,persistEvent.getProperties().get(AuthenticationEvent.LOGIN_TIME.name()).getValue());
        Assert.assertEquals(user,persistEvent.getProperties().get(AuthenticationEvent.USER.name()).getValue());
        Assert.assertEquals(accountDisabled,persistEvent.getProperties().get(AuthenticationEvent.ACCOUNT_DISABLED.name()).getValue());
    }
}

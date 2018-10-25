package eu.domibus.web.rest;

import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.logging.LoggingException;
import eu.domibus.core.logging.LoggingService;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResultRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * JUnit for {@link LoggingResource}
 *
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class LoggingResourceTest {

    @Tested
    LoggingResource loggingResource;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private LoggingService loggingService;

    @Test
    public void testSetLogLevel(final @Mocked LoggingLevelRO loggingLevelRO) {
        final String name = "eu.domibus";
        final String level = "DEBUG";

        new Expectations() {{
            loggingLevelRO.getName();
            result = name;

            loggingLevelRO.getLevel();
            result = level;

        }};

        //tested method
        loggingResource.setLogLevel(loggingLevelRO);

        new Verifications() {{
           loggingService.setLoggingLevel(name, level);
           times = 1;

            loggingService.signalSetLoggingLevel(name, level);
            times = 1;
        }};
    }

    @Test
    public void testGetLogLevel(final @Mocked List<LoggingEntry> loggingEntryList) {
        final String name = "eu.domibus";
        final boolean showClasses = false;

        final List<LoggingLevelRO> loggingLevelROList = new ArrayList<>();
        LoggingLevelRO loggingLevelRO1 = new LoggingLevelRO();
        loggingLevelRO1.setLevel("INFO");
        loggingLevelRO1.setName("eu.domibus");
        loggingLevelROList.add(loggingLevelRO1);
        LoggingLevelRO loggingLevelRO2 = new LoggingLevelRO();
        loggingLevelRO2.setLevel("INFO");
        loggingLevelRO2.setName("eu.domibus.common");
        loggingLevelROList.add(loggingLevelRO2);
        LoggingLevelRO loggingLevelRO3 = new LoggingLevelRO();
        loggingLevelRO3.setLevel("INFO");
        loggingLevelRO3.setName("eu.domibus.common.model");
        loggingLevelROList.add(loggingLevelRO3);

        new Expectations(loggingResource) {{
            loggingService.getLoggingLevel(name, showClasses);
            result = loggingEntryList;

            domainConverter.convert(loggingEntryList, LoggingLevelRO.class);
            result = loggingLevelROList;

        }};

        //tested method
        final ResponseEntity<LoggingLevelResultRO>  result =
                loggingResource.getLogLevel(name, showClasses, 0, 2, null, false);

        Assert.assertNotNull(result.getBody().getLoggingEntries());
        List<LoggingLevelRO> loggingEntries = result.getBody().getLoggingEntries();
        Assert.assertTrue(!loggingEntries.isEmpty());
        Assert.assertEquals(2, loggingEntries.size());
    }

    @Test
    public void testResetLogging() {

        //tested method
        loggingResource.resetLogging();

        new Verifications() {{
            loggingService.resetLogging();
        }};

    }


    @Test
    public void testHandleLoggingException() {

        final LoggingException loggingException = new LoggingException("error while setting log level");

        //tested method
        final ErrorRO result = loggingResource.handleLoggingException(loggingException);
        Assert.assertNotNull(result);
        Assert.assertEquals(loggingException.getMessage(), result.getMessage());
    }
}
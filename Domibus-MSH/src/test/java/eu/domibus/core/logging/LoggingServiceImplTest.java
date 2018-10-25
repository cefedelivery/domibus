package eu.domibus.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.core.converter.DomainCoreConverter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Topic;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class LoggingServiceImplTest {

    @Injectable
    protected DomainCoreConverter domainConverter;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected Topic clusterCommandTopic;

    @Injectable
    protected JMSManager jmsManager;

    @Tested
    LoggingServiceImpl loggingService;

    @Test
    public void testSetLoggingLevel_LevelNotNull_LoggerLevelSet(final @Mocked LoggerContext loggerContext) {
        final String name = "eu.domibus";
        final String level = "DEBUG";

        //LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        new Expectations(loggingService) {{

        }};

        //tested method
        loggingService.setLoggingLevel(name, level);

        new Verifications() {{
            Level actualLevel;
            loggerContext.getLogger(name).setLevel(actualLevel = withCapture());
        }};

    }

    @Test
    public void signalSetLoggingLevel() {
    }

    @Test
    public void getLoggingLevel() {
    }

    @Test
    public void resetLogging() {
    }

    @Test
    public void signalResetLogging() {
    }
}
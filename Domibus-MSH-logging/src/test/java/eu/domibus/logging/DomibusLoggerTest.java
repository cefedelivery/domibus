package eu.domibus.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class DomibusLoggerTest {

    @Test
    public void testMDC() throws Exception {
        final DomibusLogger domibusLogger = DomibusLoggerFactory.getLogger(DomibusLoggerTest.class.getName());
        final String key = "key1";
        final String value = "value1";
        domibusLogger.putMDC(key, value);
        assertTrue(domibusLogger.getCopyOfContextMap().containsKey("d_key1"));
        assertEquals(domibusLogger.getMDC(key), value);
        domibusLogger.removeMDC(key);
        assertFalse(domibusLogger.getCopyOfContextMap().containsKey("d_key1"));

        domibusLogger.putMDC(key, value);
        assertTrue(domibusLogger.getCopyOfContextMap().containsKey("d_key1"));
        domibusLogger.clearCustomKeys();
        assertFalse(domibusLogger.getCopyOfContextMap().containsKey("d_key1"));
    }

    @Test
    public void testLoggerMethods() throws Exception {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.setContext(root.getLoggerContext());
        final Level originalRootLevel = root.getLevel();

        root.setLevel(Level.TRACE);
        listAppender.setName("mylistappender");
        listAppender.start();
        root.addAppender(listAppender);

        final DomibusLogger domibusLogger = DomibusLoggerFactory.getLogger(DomibusLoggerTest.class);
        final String charset = "UTF-8";
        domibusLogger.businessTrace(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, charset);
        domibusLogger.businessDebug(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, charset);
        domibusLogger.businessInfo(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, charset);
        domibusLogger.businessWarn(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, charset);
        domibusLogger.businessError(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, charset);

        String domibusMessageLogSuffix = "[BUS-005] Invalid charset [UTF-8] used";
        final String businessMessageLogSuffix = "[BUSINESS - BUS-005] Invalid charset [UTF-8] used";
        List<String> expectedLogs = new ArrayList<>();
        expectedLogs.add("[TRACE] " + domibusMessageLogSuffix);
        expectedLogs.add("[TRACE] " + businessMessageLogSuffix);
        expectedLogs.add("[DEBUG] " + domibusMessageLogSuffix);
        expectedLogs.add("[DEBUG] " + businessMessageLogSuffix);
        expectedLogs.add("[INFO] " + domibusMessageLogSuffix);
        expectedLogs.add("[INFO] " + businessMessageLogSuffix);
        expectedLogs.add("[WARN] " + domibusMessageLogSuffix);
        expectedLogs.add("[WARN] " + businessMessageLogSuffix);
        expectedLogs.add("[ERROR] " + domibusMessageLogSuffix);
        expectedLogs.add("[ERROR] " + businessMessageLogSuffix);

        final List<ILoggingEvent> list = listAppender.list;
        assertEqualLogs(list, expectedLogs);

        expectedLogs.clear();
        list.clear();
        domibusMessageLogSuffix = "[SEC-002] Basic authentication is used";
        final String securityMessageLogSuffix = "[SECURITY - SEC-002] Basic authentication is used";
        expectedLogs.add("[TRACE] " + domibusMessageLogSuffix);
        expectedLogs.add("[TRACE] " + securityMessageLogSuffix);
        expectedLogs.add("[DEBUG] " + domibusMessageLogSuffix);
        expectedLogs.add("[DEBUG] " + securityMessageLogSuffix);
        expectedLogs.add("[INFO] " + domibusMessageLogSuffix);
        expectedLogs.add("[INFO] " + securityMessageLogSuffix);
        expectedLogs.add("[WARN] " + domibusMessageLogSuffix);
        expectedLogs.add("[WARN] " + securityMessageLogSuffix);
        expectedLogs.add("[ERROR] " + domibusMessageLogSuffix);
        expectedLogs.add("[ERROR] " + securityMessageLogSuffix);
        domibusLogger.securityTrace(DomibusMessageCode.SEC_BASIC_AUTHENTICATION_USE);
        domibusLogger.securityDebug(DomibusMessageCode.SEC_BASIC_AUTHENTICATION_USE);
        domibusLogger.securityInfo(DomibusMessageCode.SEC_BASIC_AUTHENTICATION_USE);
        domibusLogger.securityWarn(DomibusMessageCode.SEC_BASIC_AUTHENTICATION_USE);
        domibusLogger.securityError(DomibusMessageCode.SEC_BASIC_AUTHENTICATION_USE);
        assertEqualLogs(list, expectedLogs);

        root.setLevel(originalRootLevel);
    }

    protected void assertEqualLogs(List<ILoggingEvent> list, List<String> expectedLogs) {
        assertEquals(list.size(), expectedLogs.size());
        assertEquals(loggingEventsToStringList(list), expectedLogs);
    }

    protected List<String> loggingEventsToStringList(List<ILoggingEvent> list) {
        List<String> logs = new ArrayList<>();
        for (ILoggingEvent iLoggingEvent : list) {
            logs.add(iLoggingEvent.toString());
        }
        return logs;
    }
}

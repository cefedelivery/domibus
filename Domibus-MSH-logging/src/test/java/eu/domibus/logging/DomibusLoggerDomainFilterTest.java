package eu.domibus.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * JUnit for {@link DomibusLoggerDomainFilter} class
 *
 * @author Catalin Enache
 * @since 4.0
 */
@RunWith(JMockit.class)
public class DomibusLoggerDomainFilterTest {

    @Tested
    DomibusLoggerDomainFilter domibusLoggerDomainFilter;

    @Test
    public void testDecide_NoDomainConfigured_NoMdcDomain_FilterMatch(final @Mocked ILoggingEvent iLoggingEvent) {
        final Map<String, String> mdcPropertyMap = new HashMap<>();
        mdcPropertyMap.put(DomibusLoggerDomainFilter.MDC_DOMAIN_KEY, null);

        new Expectations() {
            {
                domibusLoggerDomainFilter.setDomain(null);
                domibusLoggerDomainFilter.setOnMatch("NEUTRAL");
                domibusLoggerDomainFilter.setOnMismatch("DENY");

                iLoggingEvent.getMDCPropertyMap();
                result = mdcPropertyMap;

            }
        };

        //tested method
        Assert.assertEquals(FilterReply.NEUTRAL, domibusLoggerDomainFilter.decide(iLoggingEvent));
    }

    @Test
    public void testDecide_NoDomainConfigured_ExistsMdcDomain_FilterMismatch(final @Mocked ILoggingEvent iLoggingEvent) {
        final Map<String, String> mdcPropertyMap = new HashMap<>();
        mdcPropertyMap.put(DomibusLoggerDomainFilter.MDC_DOMAIN_KEY, "default");

        new Expectations() {
            {
                domibusLoggerDomainFilter.setDomain(null);
                domibusLoggerDomainFilter.setOnMatch("ACCEPT");
                domibusLoggerDomainFilter.setOnMismatch("NEUTRAL");

                iLoggingEvent.getMDCPropertyMap();
                result = mdcPropertyMap;

            }
        };

        //tested method
        Assert.assertEquals(FilterReply.NEUTRAL, domibusLoggerDomainFilter.decide(iLoggingEvent));
    }

    @Test
    public void testDecide_DomainConfigured_ExistsMdcDomain_FilterMismatch(final @Mocked ILoggingEvent iLoggingEvent) {
        Map<String, String> mdcMap = new HashMap<>();
        mdcMap.put(DomibusLoggerDomainFilter.MDC_DOMAIN_KEY, "taxud");

        new Expectations() {
            {
                domibusLoggerDomainFilter.setDomain("default");
                domibusLoggerDomainFilter.setOnMatch("DENY");
                domibusLoggerDomainFilter.setOnMismatch("ACCEPT");

                iLoggingEvent.getMDCPropertyMap();
                result = mdcMap;

            }
        };

        //tested method
        Assert.assertEquals(FilterReply.ACCEPT, domibusLoggerDomainFilter.decide(iLoggingEvent));
    }

}
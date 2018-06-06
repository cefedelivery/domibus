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
import org.slf4j.MarkerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

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
    public void testDecide_DomainNotPresent_FilterDeny(final @Mocked ILoggingEvent iLoggingEvent) {

        new Expectations() {
            {
                domibusLoggerDomainFilter.setDomainName("default");
                domibusLoggerDomainFilter.setMarkerName("BUSINESS,SECURITY");
                domibusLoggerDomainFilter.setMarkerMatch("NEUTRAL");
                domibusLoggerDomainFilter.setMarkerMismatch("DENY");

                iLoggingEvent.getMDCPropertyMap();
                result = null;

            }
        };

        //tested method
        Assert.assertEquals(FilterReply.DENY, domibusLoggerDomainFilter.decide(iLoggingEvent));
    }

    @Test
    public void testDecide_DomainPresent_FilterDeny(final @Mocked ILoggingEvent iLoggingEvent) {
        Map<String, String> mdcMap = new HashMap<>();
        mdcMap.put(DomibusLoggerDomainFilter.MDC_DOMAIN_KEY, "taxud");

        new Expectations() {
            {
                domibusLoggerDomainFilter.setDomainName("default");
                domibusLoggerDomainFilter.setMarkerName("BUSINESS,SECURITY");
                domibusLoggerDomainFilter.setMarkerMatch("NEUTRAL");
                domibusLoggerDomainFilter.setMarkerMismatch("DENY");

                iLoggingEvent.getMDCPropertyMap();
                result = mdcMap;

            }
        };

        //tested method
        Assert.assertEquals(FilterReply.DENY, domibusLoggerDomainFilter.decide(iLoggingEvent));
    }

    @Test
    public void testDecide_DomainPresent_MarkerMatch_FilterAccept(final @Mocked ILoggingEvent iLoggingEvent) {
        Map<String, String> mdcMap = new HashMap<>();
        final String domainName = "taxud";
        mdcMap.put(DomibusLoggerDomainFilter.MDC_DOMAIN_KEY, domainName);
        final String markerMatch = "NEUTRAL";

        new Expectations() {
            {
                domibusLoggerDomainFilter.setDomainName(domainName);
                domibusLoggerDomainFilter.setMarkerName("BUSINESS,SECURITY");
                domibusLoggerDomainFilter.setMarkerMatch(markerMatch);
                domibusLoggerDomainFilter.setMarkerMismatch("DENY");

                iLoggingEvent.getMDCPropertyMap();
                result = mdcMap;

                iLoggingEvent.getMarker();
                result = MarkerFactory.getMarker("BUSINESS");

            }
        };

        //tested method
        Assert.assertEquals(FilterReply.valueOf(markerMatch), domibusLoggerDomainFilter.decide(iLoggingEvent));
    }


    @Test
    public void testDecide_DomainPresent_MarkerMismatch_FilterDeny(final @Mocked ILoggingEvent iLoggingEvent) {
        Map<String, String> mdcMap = new HashMap<>();
        final String domainName = "taxud";
        mdcMap.put(DomibusLoggerDomainFilter.MDC_DOMAIN_KEY, domainName);
        final String markerMismatch = "DENY";

        new Expectations() {
            {
                domibusLoggerDomainFilter.setDomainName(domainName);
                domibusLoggerDomainFilter.setMarkerName("BUSINESS,SECURITY");
                domibusLoggerDomainFilter.setMarkerMatch("NEUTRAL");
                domibusLoggerDomainFilter.setMarkerMismatch(markerMismatch);

                iLoggingEvent.getMDCPropertyMap();
                result = mdcMap;

                iLoggingEvent.getMarker();
                result = MarkerFactory.getMarker("BUSINESS1");

            }
        };

        //tested method
        Assert.assertEquals(FilterReply.valueOf(markerMismatch), domibusLoggerDomainFilter.decide(iLoggingEvent));
    }
}
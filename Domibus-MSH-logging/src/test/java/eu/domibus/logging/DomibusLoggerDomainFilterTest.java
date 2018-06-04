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

import static org.junit.Assert.*;

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

            }};

        //tested method
        Assert.assertEquals(FilterReply.DENY, domibusLoggerDomainFilter.decide(iLoggingEvent));
    }
}
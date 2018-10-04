package eu.domibus.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.MatchingFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;
import org.slf4j.Marker;


/**
 *
 */
public class DomibusLoggerDomainTurboFilter extends MatchingFilter {

    static final String MDC_DOMAIN_KEY = DomibusLogger.MDC_PROPERTY_PREFIX + DomibusLogger.MDC_DOMAIN;

    private String domainName;

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }




    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String s, Object[] objects, Throwable throwable) {

        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        return domainName != null && domainName.equals(MDC.get(MDC_DOMAIN_KEY)) ? this.onMatch : this.onMismatch;
    }

}

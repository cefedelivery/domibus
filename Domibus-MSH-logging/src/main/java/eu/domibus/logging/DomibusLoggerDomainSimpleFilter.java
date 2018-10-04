package eu.domibus.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Catalin Enache
 * since 4.0.1
 */
public class DomibusLoggerDomainSimpleFilter extends Filter<ILoggingEvent> {
    static final String MDC_DOMAIN_KEY = DomibusLogger.MDC_PROPERTY_PREFIX + DomibusLogger.MDC_DOMAIN;

    private FilterReply onMatch;
    private FilterReply onMismatch;
    private String domain;


    public DomibusLoggerDomainSimpleFilter() {
        this.onMatch = FilterReply.NEUTRAL;
        this.onMismatch = FilterReply.NEUTRAL;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public final void setOnMatch(String action) {
        if ("NEUTRAL".equals(action)) {
            this.onMatch = FilterReply.NEUTRAL;
        } else if ("ACCEPT".equals(action)) {
            this.onMatch = FilterReply.ACCEPT;
        } else if ("DENY".equals(action)) {
            this.onMatch = FilterReply.DENY;
        }
    }

    public final void setOnMismatch(String action) {
        if ("NEUTRAL".equals(action)) {
            this.onMismatch = FilterReply.NEUTRAL;
        } else if ("ACCEPT".equals(action)) {
            this.onMismatch = FilterReply.ACCEPT;
        } else if ("DENY".equals(action)) {
            this.onMismatch = FilterReply.DENY;
        }
    }


    @Override
    public FilterReply decide(ILoggingEvent event) {

        if (StringUtils.isBlank(domain)) {
            return FilterReply.NEUTRAL;
        }

        //get the domain
        String value = event.getMDCPropertyMap().get(MDC_DOMAIN_KEY);

        return domain.equals(value) ? this.onMatch : this.onMismatch;
    }


}

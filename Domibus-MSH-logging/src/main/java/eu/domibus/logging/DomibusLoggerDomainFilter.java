package eu.domibus.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Map;

/**
 * Implementation of {@link Filter} for Domain logging
 * If the MDC map is containing the domain key then the filter will proceed to check the markers
 *
 * @author Catalin Enache
 * @since 4.0
 */
public class DomibusLoggerDomainFilter extends Filter<ILoggingEvent> {

    static final String MDC_DOMAIN_KEY = "d_domain";
    private String domainName = "default";

    private String markerName = "LOGGED_MARKER";
    private String markerMatch = "NEUTRAL";
    private String markerMismatch = "DENY";

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

    public void setMarkerMatch(String markerMatch) {
        this.markerMatch = markerMatch;
    }

    public void setMarkerMismatch(String markerMismatch) {
        this.markerMismatch = markerMismatch;
    }

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {

        Map<String, String> mdcPropertyMap = iLoggingEvent.getMDCPropertyMap();
        Marker markerToAccept = MarkerFactory.getMarker(markerName);

        //filter by domain from MDC map
        if (mdcPropertyMap != null && mdcPropertyMap.get(MDC_DOMAIN_KEY) != null) {
            if (domainName.equals(mdcPropertyMap.get(MDC_DOMAIN_KEY))) {

                //filter by marker
                if (markerToAccept.equals(iLoggingEvent.getMarker())) {
                    return FilterReply.valueOf(markerMatch);
                } else {
                    return FilterReply.valueOf(markerMismatch);
                }
            }
        }

        return FilterReply.DENY;
    }

}

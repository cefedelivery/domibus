package eu.domibus.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link Filter} for Domain logging
 * If the MDC map is containing the domain key then the filter will proceed to check the markers
 *
 * @author Catalin Enache
 * @since 4.0
 */
public class DomibusLoggerDomainFilter extends Filter<ILoggingEvent> {

    static final String MDC_DOMAIN_KEY = DomibusLogger.MDC_PROPERTY_PREFIX + DomibusLogger.MDC_DOMAIN;
    private static final String MARKER_SEPARATOR = ",";
    private String domainName = "default";

    private String markerName;
    private String markerMatch;
    private String markerMismatch;

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

        //MDC map
        Map<String, String> mdcPropertyMap = iLoggingEvent.getMDCPropertyMap();

        //read the configuration  - markers to check
        List<Marker> markerListToCheck = Arrays.stream(markerName.split(MARKER_SEPARATOR)).map(s -> MarkerFactory.getMarker(s)).collect(Collectors.toList());


        //filter by domain from MDC map
        if (mdcPropertyMap != null && domainName.equals(mdcPropertyMap.get(MDC_DOMAIN_KEY))) {

            //filter by marker
            if (markerListToCheck.contains(iLoggingEvent.getMarker())) {
                return FilterReply.valueOf(markerMatch);
            } else {
                return FilterReply.valueOf(markerMismatch);
            }

        }

        return FilterReply.DENY;
    }

}

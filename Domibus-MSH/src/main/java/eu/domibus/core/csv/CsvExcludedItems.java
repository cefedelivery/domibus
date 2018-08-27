package eu.domibus.core.csv;

import java.util.Arrays;
import java.util.List;

public enum CsvExcludedItems {
    USER_RESOURCE(new String[]{"authorities", "status", "password", "suspended", "domain"}),
    PLUGIN_USER_RESOURCE(new String[]{"entityId", "status", "passwd", "domain"}),
    PMODE_RESOURCE(new String[]{"id"}),
    PARTY_RESOURCE(new String[]{"entityId", "identifiers", "userName", "processesWithPartyAsInitiator", "processesWithPartyAsResponder", "certificateContent"}),
    JMS_RESOURCE(new String[]{"PROPERTY_ORIGINAL_QUEUE", "jmsCorrelationId"}),
    AUDIT_RESOURCE(new String[]{"revisionId"}),
    TRUSTSTORE_RESOURCE(new String[]{"fingerprints"});


    private String[] excludedItems;

    CsvExcludedItems(final String[] excludedItems) {
        this.excludedItems = excludedItems;
    }

    public List<String> getExcludedItems() {
        return Arrays.asList(excludedItems);
    }
}

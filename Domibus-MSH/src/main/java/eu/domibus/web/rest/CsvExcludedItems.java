package eu.domibus.web.rest;

import java.util.Arrays;
import java.util.List;

public enum CsvExcludedItems {
    USER_RESOURCE(new String[] {"authorities", "status", "password", "suspended"}),
    PMODE_RESOURCE(new String[] {"id"}),
    PARTY_RESOURCE(new String[] {"entityId", "identifiers", "userName", "processesWithPartyAsInitiator", "processesWithPartyAsResponder"}),
    JMS_RESOURCE(new String[] {"PROPERTY_ORIGINAL_QUEUE"}),
    AUDIT_RESOURCE(new String[] {"revisionId"});


    private String[] excludedItems;

    CsvExcludedItems(final String[] excludedItems) {
        this.excludedItems = excludedItems;
    }

    public List<String> getExcludedItems() {
        return Arrays.asList(excludedItems);
    }
}

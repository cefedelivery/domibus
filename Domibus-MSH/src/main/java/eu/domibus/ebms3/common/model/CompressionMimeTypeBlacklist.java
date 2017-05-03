package eu.domibus.ebms3.common.model;

import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class CompressionMimeTypeBlacklist {

    private List<String> entries;

    public List<String> getEntries() {
        return this.entries;
    }

    public void setEntries(final List<String> entries) {
        this.entries = entries;
    }
}

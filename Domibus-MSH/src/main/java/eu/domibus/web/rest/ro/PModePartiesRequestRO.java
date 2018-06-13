package eu.domibus.web.rest.ro;

import eu.domibus.core.party.PartyResponseRo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.0
 *
 * Request Object used when updating the list of PMode Parties.
 */
public class PModePartiesRequestRO {

    private List<PartyResponseRo> added = new ArrayList<>();

    private List<PartyResponseRo> updated = new ArrayList<>();

    private List<PartyResponseRo> deleted = new ArrayList<>();

    public List<PartyResponseRo> getAdded() {
        return added;
    }

    public void setAdded(List<PartyResponseRo> added) {
        this.added = added;
    }

    public List<PartyResponseRo> getUpdated() {
        return updated;
    }

    public void setUpdated(List<PartyResponseRo> updated) {
        this.updated = updated;
    }

    public List<PartyResponseRo> getDeleted() {
        return deleted;
    }

    public void setDeleted(List<PartyResponseRo> deleted) {
        this.deleted = deleted;
    }

}

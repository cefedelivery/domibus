package eu.domibus.core.party;

import eu.domibus.api.party.Party;
import eu.domibus.common.dao.PartyDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PartyServiceImpl {

    @Autowired
    private PartyDao partyDao;

    public List<Party> listParties() {
        return null;
    }
}

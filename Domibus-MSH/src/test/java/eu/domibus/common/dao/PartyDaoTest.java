package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import org.junit.Test;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PartyDaoTest {
    @Test
    public void listParties() throws Exception {
        Party party = new Party();
        party.setName("P1");
        Identifier id = new Identifier();
        id.setPartyId("P1 party id");
        party.getIdentifiers().add(id);

        Process process = new Process();
        process.getInitiatorParties().add(party);

        party = new Party();
        party.setName("P2");
        id = new Identifier();
        id.setPartyId("P2 party id");
        party.getIdentifiers().add(id);

        process.getInitiatorParties().add(party);

        party = new Party();
        party.setName("P2");
        id = new Identifier();
        id.setPartyId("P3 party id");
        party.getIdentifiers().add(id);

    }

}
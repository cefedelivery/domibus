package eu.domibus.common.dao;

import eu.domibus.audit.InMemoryDataBaseConfig;
import eu.domibus.audit.OracleDataBaseConfig;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class,
        OracleDataBaseConfig.class, PartyDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class PartyDaoTest {

    @PersistenceContext
    private javax.persistence.EntityManager em;

    @Autowired
    private PartyDao partyDao;

    public void init() {
        Party party = new Party();
        party.setName("P1");
        Identifier id = new Identifier();
        id.setPartyId("P1 party id");
        party.getIdentifiers().add(id);

        em.persist(party);

        Process process = new Process();
        process.addInitiator(party);

        party = new Party();
        party.setName("P2");
        id = new Identifier();
        id.setPartyId("P2 party id");
        party.getIdentifiers().add(id);

        process.addResponder(party);

        em.persist(party);

        party = new Party();
        party.setName("P2");
        id = new Identifier();
        id.setPartyId("P3 party id");
        party.getIdentifiers().add(id);

        em.persist(party);

        em.persist(process);
    }

    @Transactional
    @Test
    public void listParties() throws Exception {
        init();
        List<Party> parties = partyDao.listParties("", "", "", "", 0, 10);
        Assert.assertEquals(3, parties.size());
    }

}
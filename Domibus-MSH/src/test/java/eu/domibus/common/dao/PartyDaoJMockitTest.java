package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.Party;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@RunWith(JMockit.class)
public class PartyDaoJMockitTest {

    @Tested
    PartyDao partyDao;

    @Injectable
    EntityManager entityManager;

    @Mocked
    Query query;

    @Test
    public void testFindById() {
        // Given
        Party party = new Party();

        new Expectations() {{
            entityManager.createNamedQuery(anyString);
            result = query;
            query.getSingleResult();
            result = party;
        }};

        // When
        Party findById = partyDao.findById("test");

        // Then
        Assert.assertEquals(party, findById);
    }
}

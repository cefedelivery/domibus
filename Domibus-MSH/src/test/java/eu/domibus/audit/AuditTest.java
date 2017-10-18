package eu.domibus.audit;

import eu.domibus.common.model.security.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;
import java.sql.SQLException;

/**
 * Created by hykiukira on 04/10/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class,
                    OracleDataBaseConfig.class})
@ActiveProfiles("ORACLE_DATABASE")
public class AuditTest {

    @PersistenceContext
    private javax.persistence.EntityManager em;

    @Test
    @Transactional
    public void testSaveEntity() throws SQLException {
        User user=new User();
        user.setUserName("Test33");
        user.setEmail("dussart.thomas@gmail.com");
        user.setPassword("test");
        user.setActive(true);
        em.persist(user);

      /*  Configuration configuration = new Configuration();
        configuration.setMpcs(new HashSet<Mpc>());
        BusinessProcesses businessProcesses = new BusinessProcesses();

        configuration.setBusinessProcesses(businessProcesses);
        Party party = new Party();
        party.setName("Test1");
        configuration.setParty(party);
        em.persist(configuration);

        TypedQuery<RevisionLog> query = em.createQuery("From RevisionLog", RevisionLog.class);
        List<RevisionLog> resultList = query.getResultList();
        Assert.assertEquals(2, resultList.size());*/
        /*AuditReader auditReader = AuditReaderFactory.get(em);
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(AbstractBaseEntity.class, false, false);
        List resultList = query.getResultList();
        for (Object o : resultList) {

        }*/
      /*  User user1 = em.find(User.class, 568);
        UserRole userRole = em.find(UserRole.class, 1);
        user1.addRole(userRole);
        em.persist(user1);*/

    }

}

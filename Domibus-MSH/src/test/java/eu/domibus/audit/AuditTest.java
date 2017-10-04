package eu.domibus.audit;

import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Commit;
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
    @Commit
    public void testSaveEntity() throws SQLException {
        User user=new User();
        user.setUserName("Test5");
        user.setEmail("dussart.thomas@gmail.com");
        user.setPassword("test");
        user.setActive(true);
        em.persist(user);
       /* User user1 = em.find(User.class, 568);
        UserRole userRole = em.find(UserRole.class, 1);
        user1.addRole(userRole);
        em.persist(user1);*/

    }

}

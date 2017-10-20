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

    //juste inserting audited entity to verify that envers does not cause any problems.
    @Test
    @Transactional
    public void testSaveEntity() throws SQLException {
        User user=new User();
        user.setUserName("Test33");
        user.setEmail("dussart.thomas@gmail.com");
        user.setPassword("test");
        user.setActive(true);
        em.persist(user);
        //TODO add the other entities here.(Configuration/Message filter)
    }

}

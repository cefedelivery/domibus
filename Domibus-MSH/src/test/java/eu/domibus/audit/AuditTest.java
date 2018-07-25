package eu.domibus.audit;

import eu.domibus.common.model.security.User;
import eu.domibus.dao.InMemoryDataBaseConfig;
import eu.domibus.dao.OracleDataBaseConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class,
        OracleDataBaseConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class AuditTest {

    @PersistenceContext
    private javax.persistence.EntityManager em;

    //just inserting audited entity to verify that envers does not cause any problems.
    @Test
    @Transactional
    public void testSaveEntity() {
        User user=new User();
        user.setUserName("Test33");
        user.setEmail("dussart.thomas@gmail.com");
        user.setPassword("test");
        user.setActive(true);
        em.persist(user);
        //TODO add the other entities here.(Configuration/Message filter)
    }

}

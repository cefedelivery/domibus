package eu.domibus.common.dao;

import eu.domibus.audit.InMemoryDataBaseConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.PersistenceContext;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        InMemoryDataBaseConfig.class,
        AuditDaoTestConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class AuditDaoImplTest {

    @PersistenceContext
    private javax.persistence.EntityManager em;

    @Autowired
    private AuditDao auditDao;

    @Autowired
    private TransactionTemplate transactionTemplate;




    @Test
    public void updateUpgradeInfo() throws Exception {
        //new view
        //new cache
        //new tables

    }

    @Test
    public void updateChangeInfo() throws Exception {
        //new view
        //new cache
        //new tables
        //change in domibus.properties
        //change in ehcache.

    }


}
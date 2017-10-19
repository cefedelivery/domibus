package eu.domibus.common.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuditDaoTestConfig {

    @Bean
    public AuditDao getAuditDao() {
        return new AuditDaoImpl();
    }

    @Bean
    public TransactionTemplate getTransactionTemplate(JpaTransactionManager jpaTransactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(jpaTransactionManager);
        return transactionTemplate;
    }


}

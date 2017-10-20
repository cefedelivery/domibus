package eu.domibus.common.aspect;

import eu.domibus.ebms3.common.model.AbstractBaseAuditEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Date;

import static eu.domibus.logging.DomibusLogger.MDC_USER;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * In order to perform auditing, we are using hibernate envers to keep track of changes for most of our entities.
 * However in order to avoid degrading performance of the application, we do a simpler kind of auditing on entities that are participating in heavy
 * throughput (like messages). This aspect is in charge of this simple auditing mechanism.
 */
@Aspect
@Component
public class BasicAuditAspect {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BasicAuditAspect.class);

    /**
     * Add creation/modification dates, creation/modification user.
     *
     * @param auditEntity the entity to be modified
     */
    @Before("@annotation(eu.domibus.common.model.common.BasicAudit) && args(auditEntity)")
    public void addBasicAudit(AbstractBaseAuditEntity auditEntity) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding basic audit to entity " + auditEntity + " whith id " + auditEntity.getEntityId());
        }
        final String userName = LOG.getMDC(MDC_USER);
        final Date currentDate = new Date(System.currentTimeMillis());
        if (auditEntity.getEntityId() == 0) {
            auditEntity.setCreatedBy(userName);
        }
    }
}

package eu.domibus.common.aspect;

import eu.domibus.common.model.logging.UserMessageLog;
import org.junit.Test;

import static eu.domibus.logging.DomibusLogger.MDC_USER;
import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class BasicAuditAspectTest {

    @Test
    public void addBasicAudit() {
        BasicAuditAspect basicAuditAspect = new BasicAuditAspect();
        basicAuditAspect.LOG.putMDC(MDC_USER, "thomas");
        UserMessageLog auditEntity = new UserMessageLog();
        basicAuditAspect.addBasicAudit(auditEntity);
        assertEquals("thomas", auditEntity.getCreatedBy());
    }

}
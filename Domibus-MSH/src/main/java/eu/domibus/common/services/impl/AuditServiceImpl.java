package eu.domibus.common.services.impl;

import eu.domibus.api.audit.AuditCriteria;
import eu.domibus.api.audit.AuditLog;
import eu.domibus.common.services.AuditService;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 * {@inheritDoc}
 */
public class AuditServiceImpl implements AuditService {
    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuditLog> listAudit(AuditCriteria auditCriteria) {
        return null;
    }
}

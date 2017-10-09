package eu.domibus.common.services;

import eu.domibus.api.audit.AuditCriteria;
import eu.domibus.api.audit.AuditLog;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Service in charge of retrieving the audit logs.
 * The audit component will track historical changes on a set of tables.
 */
public interface AuditService {

    /**
     * Retrieve the list of audit for the given criterias.
     *
     * @param auditCriteria the criteria.
     * @return the list of audit.
     */
    List<AuditLog> listAudit(AuditCriteria auditCriteria);

}


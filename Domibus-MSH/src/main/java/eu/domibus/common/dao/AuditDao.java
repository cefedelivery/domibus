package eu.domibus.common.dao;

import eu.domibus.common.model.audit.Audit;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AuditDao {

    List<Audit> listAudit(Set<String> auditTargets,
                          Set<String> actions,
                          Set<String> users,
                          Date from,
                          Date to,
                          int start,
                          int max);

}

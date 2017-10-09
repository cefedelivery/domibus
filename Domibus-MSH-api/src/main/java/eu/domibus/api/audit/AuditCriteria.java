package eu.domibus.api.audit;

import java.util.Date;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuditCriteria {

    private Set<String> auditTargetName;

    private Set<String> action;

    private Set<String> user;

    private Date from;

    private Date to;

    public AuditCriteria(Set<String> auditTargetName, Set<String> action, Set<String> user, Date from, Date to) {
        this.auditTargetName = auditTargetName;
        this.action = action;
        this.user = user;
        this.from = from;
        this.to = to;
    }
}

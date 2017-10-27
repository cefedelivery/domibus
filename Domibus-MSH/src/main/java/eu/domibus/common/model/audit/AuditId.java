package eu.domibus.common.model.audit;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * Composite primary key needed for {@link Audit} because it is map to a view.
 */
@Embeddable
public class AuditId implements Serializable {

    @Column(name = "ID", updatable = false, nullable = false)
    private String id;

    @Column(name = "REV_ID", updatable = false, nullable = false)
    private String revisionId;

    @Column(name = "AUDIT_TYPE", updatable = false, nullable = false)
    private String auditTargetName;

    @Column(name = "ACTION_TYPE")
    private String action;

    public String getId() {
        return id;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public String getAuditTargetName() {
        return auditTargetName;
    }

    public String getAction() {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuditId auditId = (AuditId) o;

        if (!id.equals(auditId.id)) return false;
        if (!revisionId.equals(auditId.revisionId)) return false;
        if (!auditTargetName.equals(auditId.auditTargetName)) return false;
        return action.equals(auditId.action);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + revisionId.hashCode();
        result = 31 * result + auditTargetName.hashCode();
        result = 31 * result + action.hashCode();
        return result;
    }
}
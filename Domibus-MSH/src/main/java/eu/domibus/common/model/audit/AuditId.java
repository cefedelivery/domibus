package eu.domibus.common.model.audit;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Embeddable
public class AuditId implements Serializable {

    @Column(name = "ID", updatable = false, nullable = false)
    private String id;

    @Column(name = "REV_ID", updatable = false, nullable = false)
    private String revisionId;

    @Column(name = "AUDIT_TYPE", updatable = false, nullable = false)
    private String auditTargetName;

    public String getId() {
        return id;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public String getAuditTargetName() {
        return auditTargetName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuditId auditId = (AuditId) o;

        if (!id.equals(auditId.id)) return false;
        if (!revisionId.equals(auditId.revisionId)) return false;
        return auditTargetName.equals(auditId.auditTargetName);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + revisionId.hashCode();
        result = 31 * result + auditTargetName.hashCode();
        return result;
    }
}

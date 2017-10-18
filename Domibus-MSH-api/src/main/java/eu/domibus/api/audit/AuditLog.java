package eu.domibus.api.audit;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuditLog {

    private String id;

    private String revisionId;

    private String auditTargetName;

    private String action;

    private String user;

    private Date changed;

    public AuditLog() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public void setAuditTargetName(String auditTargetName) {
        this.auditTargetName = auditTargetName;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setChanged(Date changed) {
        this.changed = changed;
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

    public String getUser() {
        return user;
    }

    public Date getChanged() {
        return changed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuditLog auditLog = (AuditLog) o;

        if (revisionId != null ? !revisionId.equals(auditLog.revisionId) : auditLog.revisionId != null) return false;
        if (!auditTargetName.equals(auditLog.auditTargetName)) return false;
        if (!action.equals(auditLog.action)) return false;
        if (!user.equals(auditLog.user)) return false;
        return changed.equals(auditLog.changed);
    }

    @Override
    public int hashCode() {
        int result = revisionId != null ? revisionId.hashCode() : 0;
        result = 31 * result + auditTargetName.hashCode();
        result = 31 * result + action.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + changed.hashCode();
        return result;
    }
}

package eu.domibus.web.rest.ro;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuditResponseRo {
    // order of the fields is important for CSV generation

    private String auditTargetName;

    private String user;

    private String action;

    private Date changed;

    private String id;

    private String revisionId;

    public AuditResponseRo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public String getAuditTargetName() {
        return auditTargetName;
    }

    public void setAuditTargetName(String auditTargetName) {
        this.auditTargetName = auditTargetName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getChanged() {
        return changed;
    }

    public void setChanged(Date changed) {
        this.changed = changed;
    }
}

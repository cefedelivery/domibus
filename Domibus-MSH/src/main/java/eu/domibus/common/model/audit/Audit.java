package eu.domibus.common.model.audit;

import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Immutable
@Table(name = "V_AUDIT")
public class Audit {


    //needed for dozer.
    public Audit() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "REV_ID", updatable = false, nullable = false)
    private String revisionId;

    @Column(name = "AUDIT_TYPE")
    private String auditTargetName;

    @Column(name = "ACTION_TYPE")
    private String action;

    @Column(name = "USER_NAME")
    private String user;

    @Column(name = "AUDIT_DATE")
    private Date changed;

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
}

package eu.domibus.common.model.audit;

import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Immutable
@Table(name = "V_AUDIT")
public class Audit {

    @EmbeddedId()
    private AuditId id;

    @Column(name = "ACTION_TYPE")
    private String action;

    @Column(name = "USER_NAME")
    private String user;

    @Column(name = "AUDIT_DATE")
    private Date changed;

    //needed for dozer.
    public Audit() {
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

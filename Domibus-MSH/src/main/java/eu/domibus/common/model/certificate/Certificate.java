package eu.domibus.common.model.certificate;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;
import java.util.Objects;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_CERTIFICATE",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"CERTIFICATE_ALIAS"}
                )
        }
)
public class Certificate extends AbstractBaseEntity {

    @Column(name = "CERTIFICATE_ALIAS")
    private String alias;

    @Column(name = "NOT_VALID_BEFORE_DATE")
    private Date notBefore;

    @Column(name = "NOT_VALID_AFTER_DATE")
    private Date notAfter;

    @Column(name = "REVOKE_NOTIFICATION_DATE")
    private Date lastNotification;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    public Date getLastNotification() {
        return lastNotification;
    }

    public void setLastNotification(Date lastNotification) {
        this.lastNotification = lastNotification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Certificate that = (Certificate) o;
        return Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), alias);
    }
}

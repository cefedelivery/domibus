package eu.domibus.common.model.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.hibernate.envers.NotAudited;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

@MappedSuperclass
public class UserEntityBaseImpl extends AbstractBaseEntity {

    @NotNull
    @Column(name = "USER_ENABLED")
    private Boolean active = true;

    @Column(name = "ATTEMPT_COUNT")
    @NotAudited
    private Integer attemptCount = 0;

    @Column(name = "SUSPENSION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @NotAudited
    private Date suspensionDate;

    @NotNull
    @Column(name = "DEFAULT_PASSWORD")
    private Boolean defaultPassword = false;

    @Column(name = "PASSWORD_CHANGE_DATE")
    private LocalDateTime passwordChangeDate;

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }
    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Date getSuspensionDate() {
        return suspensionDate;
    }
    public void setSuspensionDate(Date suspensionDate) {
        this.suspensionDate = suspensionDate;
    }

    public LocalDateTime getPasswordChangeDate() {
        return passwordChangeDate;
    }
    public void setPasswordChangeDate(LocalDateTime passwordChangeDate) {
        this.passwordChangeDate = passwordChangeDate;
    }

    public Boolean hasDefaultPassword() {
        return this.defaultPassword;
    }
    public void setDefaultPassword(Boolean defaultPassword) {
        this.defaultPassword = defaultPassword;
    }
}

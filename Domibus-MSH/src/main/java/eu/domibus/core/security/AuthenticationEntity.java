package eu.domibus.core.security;

import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "TB_AUTHENTICATION_ENTRY")
@NamedQueries({
        @NamedQuery(name = "AuthenticationEntity.findByUsername", query = "select bae from AuthenticationEntity bae where bae.username=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntity.findByCertificateId", query = "select bae from AuthenticationEntity bae where bae.certificateId=:CERTIFICATE_ID"),
        @NamedQuery(name = "AuthenticationEntity.getRolesForUsername", query = "select bae.authRoles from AuthenticationEntity bae where bae.username=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntity.getRolesForCertificateId", query = "select bae.authRoles from AuthenticationEntity bae where bae.certificateId=:CERTIFICATE_ID")})
@NamedQuery(name = "AuthenticationEntity.findWithPasswordChangedBetween", query = "FROM AuthenticationEntity ae where ae.passwordChangeDate is not null " +
        "and ae.passwordChangeDate>:START_DATE and ae.passwordChangeDate<:END_DATE " + "and ae.defaultPassword=:DEFAULT_PASSWORD")
public class AuthenticationEntity extends AbstractBaseEntity implements UserEntityBase {

    @Column(name = "CERTIFICATE_ID")
    private String certificateId;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "PASSWD")
    private String password;
    @Column(name = "AUTH_ROLES")
    private String authRoles; // semicolon separated roles
    @Column(name = "ORIGINAL_USER")
    private String originalUser;
    @Column(name = "BACKEND")
    private String backend;

    @Column(name = "PASSWORD_CHANGE_DATE")
    private LocalDateTime passwordChangeDate;
    @NotNull
    @Column(name = "DEFAULT_PASSWORD")
    private Boolean defaultPassword = false;

    @NotNull
    @Column(name = "USER_ENABLED")
    private Boolean active = true;

    @Column(name = "ATTEMPT_COUNT")
    private Integer attemptCount = 0;

    @Column(name = "SUSPENSION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date suspensionDate;

    public AuthenticationEntity() {
        //active = true;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.passwordChangeDate = LocalDateTime.now();
    }

    public String getAuthRoles() {
        return authRoles;
    }

    public void setAuthRoles(String authRoles) {
        this.authRoles = authRoles;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    @Override
    public UserEntityBase.Type getType() {
        return Type.PLUGIN;
    }

    @Override
    public String getUserName() {
        return this.getUsername();
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

    public void setActive(Boolean enabled) {
        this.active = enabled;
    }

    public Boolean isActive() {
        return active;
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

}

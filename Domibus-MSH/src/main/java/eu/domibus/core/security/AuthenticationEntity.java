package eu.domibus.core.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_AUTHENTICATION_ENTRY")
@NamedQueries({
        @NamedQuery(name = "AuthenticationEntity.findByUsername", query = "select bae from AuthenticationEntity bae where bae.username=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntity.findByCertificateId", query = "select bae from AuthenticationEntity bae where bae.certificateId=:CERTIFICATE_ID"),
        @NamedQuery(name = "AuthenticationEntity.getRolesForUsername", query = "select bae.authRoles from AuthenticationEntity bae where bae.username=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntity.getRolesForCertificateId", query = "select bae.authRoles from AuthenticationEntity bae where bae.certificateId=:CERTIFICATE_ID")})

public class AuthenticationEntity extends AbstractBaseEntity {

    @Column(name = "CERTIFICATE_ID")
    private String certificateId;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "PASSWD")
    private String passwd;
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

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
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

    public LocalDateTime getPasswordChangeDate() { return passwordChangeDate; }

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

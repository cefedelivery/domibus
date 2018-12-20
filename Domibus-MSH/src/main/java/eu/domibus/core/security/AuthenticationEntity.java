package eu.domibus.core.security;

import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.common.model.security.UserEntityBaseImpl;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_AUTHENTICATION_ENTRY")
@NamedQueries({
        @NamedQuery(name = "AuthenticationEntity.findByUsername", query = "select bae from AuthenticationEntity bae where bae.userName=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntity.findByCertificateId", query = "select bae from AuthenticationEntity bae where bae.certificateId=:CERTIFICATE_ID"),
        @NamedQuery(name = "AuthenticationEntity.getRolesForUsername", query = "select bae.authRoles from AuthenticationEntity bae where bae.userName=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntity.getRolesForCertificateId", query = "select bae.authRoles from AuthenticationEntity bae where bae.certificateId=:CERTIFICATE_ID"),
        @NamedQuery(name = "AuthenticationEntity.findWithPasswordChangedBetween", query = "FROM AuthenticationEntity ae where ae.passwordChangeDate is not null " +
                "and ae.passwordChangeDate>:START_DATE and ae.passwordChangeDate<:END_DATE " + "and ae.defaultPassword=:DEFAULT_PASSWORD"),
        @NamedQuery(name = "AuthenticationEntity.findSuspendedUsers", query = "FROM AuthenticationEntity u where u.suspensionDate is not null and u.suspensionDate<:SUSPENSION_INTERVAL")
})
public class AuthenticationEntity extends UserEntityBaseImpl implements UserEntityBase {

    @Column(name = "CERTIFICATE_ID")
    private String certificateId;
    @Column(name = "USERNAME")
    private String userName;
    @Column(name = "PASSWD")
    private String password;
    @Column(name = "AUTH_ROLES")
    private String authRoles; // semicolon separated roles
    @Column(name = "ORIGINAL_USER")
    private String originalUser;
    @Column(name = "BACKEND")
    private String backend;

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.setPasswordChangeDate(LocalDateTime.now());
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
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

}

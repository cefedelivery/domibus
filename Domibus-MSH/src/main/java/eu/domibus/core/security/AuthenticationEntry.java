package eu.domibus.core.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "TB_AUTHENTICATION_ENTRY")
@NamedQueries({
        @NamedQuery(name = "AuthenticationEntry.findByUsername", query = "select bae from AuthenticationEntry bae where bae.username=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntry.findByCertificateId", query = "select bae from AuthenticationEntry bae where bae.certificateId=:CERTIFICATE_ID"),
        @NamedQuery(name = "AuthenticationEntry.getRolesForUsername", query = "select bae.authRoles from AuthenticationEntry bae where bae.username=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntry.getRolesForCertificateId", query = "select bae.authRoles from AuthenticationEntry bae where bae.certificateId=:CERTIFICATE_ID")})

public class AuthenticationEntry extends AbstractBaseEntity {

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
}

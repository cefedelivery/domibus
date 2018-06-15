package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Pion
 * @since 4.0
 */
public class PluginUserRO implements Serializable {

    private Integer entityId;
    private String username;
    private String passwd;
    private String certificateId;
    private String originalUser;
    private String authRoles; 
    private String authenticationType; 
    private String status;

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
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
    
    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    public String getAuthRoles() {
        return authRoles;
    }

    public void setAuthRoles(String authRoles) {
        this.authRoles = authRoles;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}

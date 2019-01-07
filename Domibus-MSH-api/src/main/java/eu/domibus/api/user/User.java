package eu.domibus.api.user;

import eu.domibus.api.security.AuthRole;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class User implements UserBase {
    private String userName;
    private String email;
    private boolean active;
    private List<String> authorities;
    private String status;
    private String password;
    private String domain;
    private boolean suspended;
    private boolean deleted;

    public User(String userName, String email, Boolean active, List<String> authorities, UserState userState,
                Date suspensionDate, boolean deleted) {
        this.userName = userName;
        this.email = email;
        this.active = active;
        this.authorities = authorities;
        this.status = userState.name();
        this.password = null;
        if (suspensionDate != null) {
            this.suspended = true;
        }
        this.deleted = deleted;
    }

    public User() {
        authorities = new LinkedList<>();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isSuperAdmin() {
        if (authorities == null) {
            return false;
        }
        return authorities.contains(AuthRole.ROLE_AP_ADMIN.name());
    }
}

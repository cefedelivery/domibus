package eu.domibus.web.rest.ro;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserResponseRO {
    private String userName;
    private String email;
    private boolean active;
    private List<String> authorities;
    private String status;
    private String password;

    public UserResponseRO(String userName, String email, boolean actif) {
        this.userName = userName;
        this.email = email;
        this.active = actif;
        this.authorities=new ArrayList<>();
    }

    public UserResponseRO() {
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public void addAuthority(String authority){
        authorities.add(authority);
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    @Override
    public String toString() {
        return "UserResponseRO{" +
                "userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", authorities=" + authorities +
                ", status='" + status + '\'' +
                '}';
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

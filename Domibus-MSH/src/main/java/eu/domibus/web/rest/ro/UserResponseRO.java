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
    private String roles="";
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

    public void updateRolesField(){
        int count=0;
        String separator="";
        for (String authority : authorities) {
            if(count>0){
               separator=",";
            }
            count++;
            roles+=separator+authority;
        }
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
                ", password='" + password+ '\'' +
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


    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getRoles() {
        return roles;
    }
}

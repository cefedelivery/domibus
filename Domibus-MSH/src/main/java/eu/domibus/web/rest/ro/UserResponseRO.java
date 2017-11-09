package eu.domibus.web.rest.ro;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
    private boolean suspended;

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

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userName", userName)
                .append("email", email)
                .append("active", active)
                .append("authorities", authorities)
                .append("roles", roles)
                .append("status", status)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserResponseRO that = (UserResponseRO) o;

        return new EqualsBuilder()
                .append(active, that.active)
                .append(userName, that.userName)
                .append(email, that.email)
                .append(authorities, that.authorities)
                .append(roles, that.roles)
                .append(status, that.status)
                .append(password, that.password)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(userName)
                .append(email)
                .append(active)
                .append(authorities)
                .append(roles)
                .append(status)
                .append(password)
                .toHashCode();
    }
}

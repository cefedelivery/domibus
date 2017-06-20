package eu.domibus.web.rest.ro;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserResponseRO {
    final private String userName;
    final private String email;
    final private boolean active;
    final private List<String> authorities;

    public UserResponseRO(String userName, String email, boolean actif) {
        this.userName = userName;
        this.email = email;
        this.active = actif;
        this.authorities=new ArrayList<>();
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
}

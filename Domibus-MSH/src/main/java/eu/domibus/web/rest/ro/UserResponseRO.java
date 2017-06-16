package eu.domibus.web.rest.ro;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserResponseRO {
    final private String userName;
    final private String role;
    final private String email;
    final private boolean actif;

    public UserResponseRO(String userName, String role, String email, boolean actif) {
        this.userName = userName;
        this.role = role;
        this.email = email;
        this.actif = actif;
    }

    public String getUserName() {
        return userName;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActif() {
        return actif;
    }
}

package eu.domibus.api.user;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class UserRole {
    private String role;

    public UserRole(String role) {
        this.role = role;
    }

    public UserRole() {

    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

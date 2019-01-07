package eu.domibus.api.user;

/**
 * @author Ion Perpegel
 * @since 4.1
 * The narrowest interface for user classes; to be able to use some common code between user entity classes and the api.User class
 */
public interface UserBase {
    String getUserName();

    boolean isActive();

    String getPassword();

    void setActive(boolean active);

    void setPassword(String password);
}

package eu.domibus.api.user;

public interface UserBase {
    String getUserName();

    boolean isActive();

    String getPassword();

    void setActive(boolean active);

    void setPassword(String password);
}

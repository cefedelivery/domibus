package eu.domibus.api.user;

public interface UserBase {
    String getUserName();

    Boolean isActive();

    String getPassword();

    void setActive(Boolean active);

    void setPassword(String password);
}

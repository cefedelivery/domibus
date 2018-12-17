package eu.domibus.common.model.security;

import eu.domibus.api.user.UserBase;

import java.time.LocalDateTime;
import java.util.Date;

public interface UserEntityBase extends UserBase {

    int getEntityId();

    UserEntityBase.Type getType();

    String getUserName();

    String getPassword();

    void setPassword(String password);

    LocalDateTime getPasswordChangeDate();

    Boolean hasDefaultPassword();

    void setDefaultPassword(Boolean defaultPassword);

    Integer getAttemptCount();

    void setAttemptCount(Integer i);

    boolean isActive();

    void setActive(boolean b);

    Date getSuspensionDate();

    void setSuspensionDate(Date suspensionDate);

    enum Type {
        CONSOLE("console_user", "Console User"),
        PLUGIN("plugin_user", "Plugin User");

        private final String code;
        private final String name;

        Type(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {

            return code;
        }

        public String getName() {
            return name;
        }
    }
}

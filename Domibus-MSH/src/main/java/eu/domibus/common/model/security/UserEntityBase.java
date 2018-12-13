package eu.domibus.common.model.security;

import eu.domibus.api.user.UserBase;

import java.time.LocalDateTime;
import java.util.Date;

public interface UserEntityBase extends UserBase {

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

    Boolean isActive();

    void setActive(Boolean b);

    Date getSuspensionDate();

    void setSuspensionDate(Date suspensionDate);

}

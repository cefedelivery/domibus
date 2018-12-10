package eu.domibus.common.model.security;

import java.time.LocalDateTime;

public interface UserBase {

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

    UserBase.Type getType();

    String getUserName();

    String getPassword();

    void setPassword(String password);

    LocalDateTime getPasswordChangeDate();

    void setDefaultPassword(Boolean defaultPassword);

    Boolean hasDefaultPassword();
}

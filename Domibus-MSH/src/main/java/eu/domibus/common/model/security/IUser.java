package eu.domibus.common.model.security;

import java.time.LocalDateTime;

public interface IUser {

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

    IUser.Type getType();

    int getEntityId();

    String getUserName();

    LocalDateTime getPasswordChangeDate();
}

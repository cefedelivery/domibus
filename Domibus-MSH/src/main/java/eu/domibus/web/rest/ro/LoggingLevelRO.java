package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * Encapsulates the package/class name and the level of logging
 *
 * @author Catalin Enache
 * @since 4.1
 */
public class LoggingLevelRO implements Serializable {

    private String name;

    private String level;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

}

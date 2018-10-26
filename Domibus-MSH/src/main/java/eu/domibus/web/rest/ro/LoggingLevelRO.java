package eu.domibus.web.rest.ro;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("level", level)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof LoggingLevelRO)) return false;

        LoggingLevelRO that = (LoggingLevelRO) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(level, that.level)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(level)
                .toHashCode();
    }
}

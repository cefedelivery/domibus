package eu.domibus.api.multitenancy;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Ion Perpegel
 * @since 4.1
 * The API class for user domain mappings in the general schema
 */
public class UserDomain {
    private String userName;
    private String domain;
    private String preferredDomain;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPreferredDomain() {
        return preferredDomain;
    }

    public void setPreferredDomain(String preferredDomain) {
        this.preferredDomain = preferredDomain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserDomain that = (UserDomain) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(userName, that.userName)
                .append(domain, that.domain)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(userName)
                .append(domain)
                .toHashCode();
    }
}

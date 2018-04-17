package eu.domibus.core.multitenancy.dao;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Entity
@Table(name = "TB_USER_DOMAIN",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"USER_NAME"}
                )
        }
)
@NamedQueries({
        @NamedQuery(name = "UserDomainEntity.findByUserName", query = "FROM UserDomainEntity u where u.userName=:USER_NAME"),
})
public class UserDomainEntity extends AbstractBaseEntity {

    @NotNull
    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "DOMAIN")
    @Email
    private String domain;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserDomainEntity that = (UserDomainEntity) o;

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

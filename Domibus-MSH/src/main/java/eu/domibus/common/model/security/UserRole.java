package eu.domibus.common.model.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "UserRole.findAll", query = "FROM UserRole"),
        @NamedQuery(name = "UserRole.findByName", query = "FROM UserRole where upper(name)=:ROLE_NAME")
})
@Table(name = "TB_USER_ROLE",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"ROLE_NAME"},
                        name = "UQ_USER_ROLE_NAME"
                )
        }
)
public class UserRole extends AbstractBaseEntity{

    @NotNull
    @Column(name = "ROLE_NAME")
    private String name;
    @ManyToMany(mappedBy = "roles",fetch = FetchType.LAZY)
    private Set<User> users=new HashSet<>();

    @SuppressWarnings("squid:S2637")
    public UserRole(String name) {
        this.name = name;
    }

    @SuppressWarnings("squid:S2637")
    public UserRole() {
    }

    public String getName() {
        return name;
    }

    public void addUser(User user){
        users.add(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserRole userRole = (UserRole) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(name, userRole.name)
                .append(users, userRole.users)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(name)
                .append(users)
                .toHashCode();
    }
}

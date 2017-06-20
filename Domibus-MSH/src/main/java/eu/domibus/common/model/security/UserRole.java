package eu.domibus.common.model.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dussath on 6/14/17.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "UserRole.findAll",
                query = "FROM UserRole")
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

    public UserRole(String name) {
        this.name = name;
    }

    public UserRole() {
    }

    public String getName() {
        return name;
    }

    public void addUser(User user){
        users.add(user);
    }
}

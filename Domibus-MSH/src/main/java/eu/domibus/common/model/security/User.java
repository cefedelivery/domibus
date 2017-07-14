package eu.domibus.common.model.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 */
@Entity
@Table(name = "TB_USER",
        uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"USER_NAME"}
        )
}
)
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "FROM User"),
        @NamedQuery(name = "User.findByUserName", query = "FROM User u where u.userName=:USER_NAME"),
        @NamedQuery(name = "User.findActiveByUserName", query = "FROM User u where u.userName=:USER_NAME and u.active=true")
})
public class User extends AbstractBaseEntity{
    @NotNull
    @Column(name = "USER_NAME")
    private String userName;
    @Column(name = "USER_EMAIL")
    @Email
    private String email;
    @NotNull
    @Column(name = "USER_PASSWORD")
    private String password;
    @NotNull
    @Column(name = "USER_ENABLED")
    private Boolean active;
    @Column(name="OPTLOCK")
    public Integer version;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "TB_USER_ROLES",
            joinColumns = @JoinColumn(
                    name = "USER_ID", referencedColumnName = "ID_PK"),
            inverseJoinColumns = @JoinColumn(
                    name = "ROLE_ID", referencedColumnName = "ID_PK"))
    private Set<UserRole> roles=new HashSet<>();

    @SuppressWarnings("squid:S2637")
    public User(@NotNull final String userName, @NotNull final String password) {
        this.userName = userName;
        this.active = Boolean.TRUE;
        this.password = password;
    }

    @SuppressWarnings("squid:S2637")
    public User() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean isEnabled() {
        return active;
    }


    public Collection<UserRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(UserRole userRole){
        roles.add(userRole);
        userRole.addUser(this);
    }

    public void clearRoles(){
        roles.clear();
    }
    public String getUserName() {
        return userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setActive(Boolean enabled) {
        this.active = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userName.equals(user.userName);
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getActive() {
        return active;
    }
}

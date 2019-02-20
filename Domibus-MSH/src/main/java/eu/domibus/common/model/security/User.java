package eu.domibus.common.model.security;

import eu.domibus.api.security.AuthRole;
import eu.domibus.common.model.common.RevisionLogicalName;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 3.3
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
        @NamedQuery(name = "User.findAll", query = "FROM User u"),
        @NamedQuery(name = "User.findByUserName", query = "FROM User u where u.userName=:USER_NAME and u.deleted=false"),
        @NamedQuery(name = "User.findActiveByUserName", query = "FROM User u where u.userName=:USER_NAME and u.active=true and u.deleted=false"),
        @NamedQuery(name = "User.findSuspendedUsers", query = "FROM User u where u.suspensionDate is not null and u.suspensionDate<:SUSPENSION_INTERVAL and u.deleted=false"),
        @NamedQuery(name = "User.findWithPasswordChangedBetween", query = "FROM User u where u.passwordChangeDate is not null and u.passwordChangeDate>:START_DATE " +
                "and u.passwordChangeDate<:END_DATE and u.defaultPassword=:DEFAULT_PASSWORD and u.deleted=false")
})

@Audited(withModifiedFlag = true)
@RevisionLogicalName("User")
public class User extends UserEntityBaseImpl implements UserEntityBase {

    @NotNull
    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "USER_EMAIL")
    @Email
    private String email;

    @NotNull
    @Column(name = "USER_PASSWORD")
    private String password;

    @Column(name = "OPTLOCK")
    public Integer version;

    @NotNull
    @Column(name = "USER_DELETED")
    private Boolean deleted = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "TB_USER_ROLES",
            joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID_PK"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID_PK"))
    private Set<UserRole> roles = new HashSet<>();

    @Override
    public UserEntityBase.Type getType() {
        return Type.CONSOLE;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.setPasswordChangeDate(LocalDateTime.now());
    }

    public Collection<UserRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(UserRole userRole) {
        roles.add(userRole);
        userRole.addUser(this);
    }

    public void clearRoles() {
        roles.clear();
    }

    public Boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isSuperAdmin() {
        if (roles == null) {
            return false;
        }
        return roles.stream().anyMatch(role -> AuthRole.ROLE_AP_ADMIN.name().equals(role.getName()));
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

}

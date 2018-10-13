package eu.domibus.common.model.security;

import eu.domibus.api.security.AuthRole;
import eu.domibus.common.model.common.RevisionLogicalName;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;

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
        @NamedQuery(name = "User.findAll", query = "FROM User u"),
        @NamedQuery(name = "User.findByUserName", query = "FROM User u where u.userName=:USER_NAME and u.deleted=false"),
        @NamedQuery(name = "User.findActiveByUserName", query = "FROM User u where u.userName=:USER_NAME and u.active=true and u.deleted=false"),
        @NamedQuery(name = "User.findSuspendedUsers", query = "FROM User u where u.suspensionDate is not null and u.suspensionDate<:SUSPENSION_INTERVAL and u.deleted=false"),
        @NamedQuery(name = "User.findWithPasswordChangedBetween", query = "FROM User u where u.passwordChangeDate is not null and u.passwordChangeDate>:START_DATE and u.passwordChangeDate<:END_DATE and u.deleted=false")
})

@Audited(withModifiedFlag = true)
@RevisionLogicalName("User")
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

    @Column(name = "ATTEMPT_COUNT")
    @NotAudited
    private Integer attemptCount = 0;

    @Column(name = "SUSPENSION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @NotAudited
    private Date suspensionDate;

    @NotNull
    @Column(name = "USER_DELETED")
    private Boolean deleted = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "TB_USER_ROLES",
            joinColumns = @JoinColumn(
                    name = "USER_ID", referencedColumnName = "ID_PK"),
            inverseJoinColumns = @JoinColumn(
                    name = "ROLE_ID", referencedColumnName = "ID_PK"))
    private Set<UserRole> roles=new HashSet<>();

    @Column(name = "PASSWORD_CHANGE_DATE")
//    @Temporal(TemporalType.TIMESTAMP)
    private LocalDate passwordChangeDate;


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

    public Boolean isDeleted() { return this.deleted; }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setActive(Boolean enabled) { this.active = enabled; }

    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

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

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Date getSuspensionDate() {
        return suspensionDate;
    }

    public void setSuspensionDate(Date suspensionDate) {
        this.suspensionDate = suspensionDate;
    }

    public LocalDate getPasswordChangeDate() { return passwordChangeDate; }

    public void setPasswordChangeDate(LocalDate passwordChangeDate) {
        this.passwordChangeDate = passwordChangeDate;
    }

    public boolean isSuperAdmin() {
        if(roles == null) {
            return false;
        }
        return roles.stream().anyMatch(role -> AuthRole.ROLE_AP_ADMIN.name().equals(role.getName()));
    }

}

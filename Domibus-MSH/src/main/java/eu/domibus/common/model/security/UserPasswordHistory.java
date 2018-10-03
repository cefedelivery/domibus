package eu.domibus.common.model.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @since 4.1
 */

@Entity
@Table(name = "TB_USER_PASSWORD_HISTORY")
@NamedQueries({
        @NamedQuery(name = "UserPasswordHistory.findPasswordDate",
                query = "SELECT passwordChangeDate FROM UserPasswordHistory WHERE user=:USER order by passwordChangeDate DESC"),
        @NamedQuery(name = "UserPasswordHistory.findPasswords",
                query = "from UserPasswordHistory where user=:USER order by passwordChangeDate DESC"),
})
public class UserPasswordHistory extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @NotNull
    @Column(name = "USER_PASSWORD")
    private String password;

    @Column(name = "PASSWORD_CHANGE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordChangeDate;

    public Date getPasswordDate() {
        return passwordChangeDate;
    }

    public UserPasswordHistory() {

    }

    public UserPasswordHistory(User user, String password) {
        this.user = user;
        this.password = password;
        this.passwordChangeDate = new Date();
    }


}

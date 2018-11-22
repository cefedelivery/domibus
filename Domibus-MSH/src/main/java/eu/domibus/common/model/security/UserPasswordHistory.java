package eu.domibus.common.model.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Entity
@Table(name = "TB_USER_PASSWORD_HISTORY")
@NamedQueries({
        @NamedQuery(name = "UserPasswordHistory.findPasswords",
                query = "from UserPasswordHistory where user=:USER order by passwordChangeDate DESC"),
})
public class UserPasswordHistory extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @NotNull
    @Column(name = "USER_PASSWORD")
    private String password; //NOSONAR

    @Column(name = "PASSWORD_CHANGE_DATE")
    private LocalDateTime passwordChangeDate;

    public String getPasswordHash() {
        return password;
    }

    public UserPasswordHistory() { }

    public UserPasswordHistory(User user, String password, LocalDateTime passwordChangeDate) {
        this.user = user;
        this.password = password;
        this.passwordChangeDate = passwordChangeDate;
    }

}

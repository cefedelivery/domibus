package eu.domibus.core.security;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Entity
@Table(name = "TB_PLUGIN_USER_PASSWORD_HISTORY")
@NamedQueries({
        @NamedQuery(name = "PluginUserPasswordHistory.findPasswords",
                query = "from PluginUserPasswordHistory where user=:USER order by passwordChangeDate DESC"),
//        @NamedQuery(name = "PluginUserPasswordHistory.findPasswordsAsStringList",
//                query = "SELECT uph.password from PluginUserPasswordHistory uph where uph.user.username=:USER_NAME order by uph.passwordChangeDate DESC"),
})
public class PluginUserPasswordHistory extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private AuthenticationEntity user;

    @NotNull
    @Column(name = "USER_PASSWORD")
    private String password; //NOSONAR

    @Column(name = "PASSWORD_CHANGE_DATE")
    private LocalDateTime passwordChangeDate;

    public String getPasswordHash() {
        return password;
    }

    public PluginUserPasswordHistory() {
    }

    public PluginUserPasswordHistory(AuthenticationEntity user, String password, LocalDateTime passwordChangeDate) {
        this.user = user;
        this.password = password;
        this.passwordChangeDate = passwordChangeDate;
    }

}

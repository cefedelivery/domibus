package eu.domibus.core.security;

import eu.domibus.common.model.security.UserPasswordHistory;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
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
})
public class PluginUserPasswordHistory extends UserPasswordHistory<AuthenticationEntity> {

    public PluginUserPasswordHistory() {
        super();
    }

    public PluginUserPasswordHistory(AuthenticationEntity user, String password, LocalDateTime passwordChangeDate) {
        super(user, password, passwordChangeDate);
    }

}

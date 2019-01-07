package eu.domibus.common.model.security;

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
@Table(name = "TB_USER_PASSWORD_HISTORY")
@NamedQueries({
        @NamedQuery(name = "ConsoleUserPasswordHistory.findPasswords",
                query = "from ConsoleUserPasswordHistory where user=:USER order by passwordChangeDate DESC"),
})
public class ConsoleUserPasswordHistory extends UserPasswordHistory<User> {

    public ConsoleUserPasswordHistory() {
        super();
    }

    public ConsoleUserPasswordHistory(User user, String password, LocalDateTime passwordChangeDate) {
        super(user, password, passwordChangeDate);
    }

}

package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.ConsoleUserPasswordHistory;
import eu.domibus.common.model.security.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Repository
public class ConsoleUserPasswordHistoryDaoImpl extends UserPasswordHistoryDaoImpl<User, ConsoleUserPasswordHistory>
        implements ConsoleUserPasswordHistoryDao {

    public ConsoleUserPasswordHistoryDaoImpl() {
        super(ConsoleUserPasswordHistory.class, "ConsoleUserPasswordHistory.findPasswords");
    }

    @Override
    protected ConsoleUserPasswordHistory createNew(final User user, String passwordHash, LocalDateTime passwordDate) {
        return new ConsoleUserPasswordHistory(user, passwordHash, passwordDate);
    }

}


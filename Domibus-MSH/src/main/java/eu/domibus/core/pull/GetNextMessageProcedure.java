package eu.domibus.core.pull;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * /**
 * @author Dussart Thomas.
 * @since 3.3.4

 * Proxy class to call the get_next stored procedure. The get_next procedure is in charge of looping through
 * messages of a given type until an unlocked message is found. When the message is found, it is locked at database level
 * and its id is returned for further processing.
 */
@Component
public class GetNextMessageProcedure {

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    /**
     * Call the procedure with the given parameters.
     * @param messageType the type of message to search for. For the moment only pull is supported.
     * @param initiator the initiator of the pull request.
     * @param mpc the mpc contained in the pull request.
     * @return the id of the locked message or null if nothing to be processed.
     */
    public String callProcedure(final String messageType, final String initiator,final String mpc) {
        MyStoredProc storedProc = new MyStoredProc(messageType, initiator,mpc);
        entityManager.unwrap(Session.class).doWork(storedProc);
        return storedProc.getMessageId();
    }

    private static final class MyStoredProc implements Work {

        private final String messageType;
        private final String initiator;
        private final String mpc;
        private String messageId;


        public MyStoredProc(final String messageType, final String initiator, final String mpc) {
            this.messageType = messageType;
            this.initiator = initiator;
            this.mpc = mpc;
        }

        @Override
        public void execute(Connection conn) throws SQLException {
            try (CallableStatement stmt = conn.prepareCall(
                    "{CALL get_next(?, ?, ?, ?)}")) {
                stmt.setString(1, messageType);
                stmt.setString(2, initiator);
                stmt.setString(3, mpc);
                stmt.registerOutParameter(4, Types.VARCHAR);
                stmt.executeUpdate();
                messageId = stmt.getString(4);
                if (stmt.wasNull()) {
                    messageId = null;
                }
            }
        }

        public String getMessageId() {
            return messageId;
        }
    }
}



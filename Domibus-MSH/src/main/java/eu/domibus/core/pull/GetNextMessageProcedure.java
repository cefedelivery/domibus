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

@Component
public class GetNextMessageProcedure {

    private final static Logger LOG = LoggerFactory.getLogger(GetNextMessageProcedure.class);

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

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
                    "{CALL get_next(:message_type, :initiator, :mpc, :message_id)}")) {
                stmt.setString("message_type", messageType);
                stmt.setString("initiator", initiator);
                stmt.setString("mpc", mpc);
                stmt.registerOutParameter("message_id", Types.VARCHAR);
                stmt.executeUpdate();
                messageId = stmt.getString("message_id");
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



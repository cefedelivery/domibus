package eu.domibus.core.pull;

import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class NextPullMessageProcedureWork implements Work {

    private final String messageType;

    private final String initiator;

    private final String mpc;

    private String messageId;

    public NextPullMessageProcedureWork(final String messageType, final String initiator, final String mpc) {
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
        }
    }

    public String getMessageId() {
        return messageId;
    }
}

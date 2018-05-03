package eu.domibus.core.pull;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.*;

@RunWith(JMockit.class)
public class NextPullMessageProcedureWorkTest {

    @Tested
    private NextPullMessageProcedureWork nextPullMessageProcedureWork;

    @Test
    public void execute(
            @Injectable("messageType") final String messageType,
            @Injectable("initiator") final String initiator,
            @Injectable("mpc") final String mpc,
            @Mocked final Connection connection,
            @Mocked final CallableStatement stmt) throws SQLException {

        new Expectations(){{
            connection.prepareCall(
                    "{CALL get_next(?, ?, ?, ?)}");
            result= stmt;
            stmt.getString(4);
            result="messageId";
        }};
        nextPullMessageProcedureWork.execute(connection);
        Assert.assertEquals("messageId",nextPullMessageProcedureWork.getMessageId());

        new Verifications(){{
            stmt.setString(1, messageType);
            stmt.setString(2, initiator);
            stmt.setString(3, mpc);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.executeUpdate();
            stmt.getString(4);

        }};
    }
}
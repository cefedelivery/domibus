package eu.domibus.core.pull;

import org.hibernate.jdbc.ReturningWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PullLockWork implements ReturningWork<Integer> {

    private final static Logger LOG = LoggerFactory.getLogger(PullLockWork.class);

    private Integer idpk;

    private final String query;

    public PullLockWork(Integer idPk, String query) {
        this.idpk = idPk;
        this.query = query;
    }

    @Override
    public Integer execute(Connection connection) throws SQLException {
        LOG.info("Query[{}] ", query);
        ResultSet resultSet = null;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idpk);
            resultSet = preparedStatement.executeQuery();
            return idpk;
        } catch (Exception ex) {
            LOG.error("Error while executing lock " + ex);
            return null;

        } finally {
            if (resultSet != null) {
                resultSet.close();
            }

        }
    }

    public Integer getIdpk() {
        return idpk;
    }
}

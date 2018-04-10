package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.crypto.api.DomainPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Conditional(MultiTenantAwareEntityManagerCondition.class)
@Service
public class DomibusMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMultiTenantConnectionProvider.class);

    @Qualifier("domibusJDBC-XADataSource")
    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainPropertyProvider domainPropertyProvider;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String identifier) throws SQLException {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        String databaseSchema = null;
        if (currentDomain != null) {
            LOG.debug("Getting schema for domain [{}]", currentDomain);
            databaseSchema = domainService.getDatabaseSchema(currentDomain);
        } else {
            LOG.debug("Getting general schema");
            databaseSchema = domainService.getGeneralSchema();
        }

        final Connection connection = getAnyConnection();
        LOG.debug("Setting database schema to [{}] ", databaseSchema);
        setSchema(connection, databaseSchema);
        return connection;
    }

    protected void setSchema(final Connection connection, String databaseSchema) throws SQLException {
        try {
            //TODO check how to set the schema dependent on MySQL or Oracle
            connection.createStatement().execute("USE " + databaseSchema);
        } catch (final SQLException e) {
            throw new HibernateException("Could not alter JDBC connection to specified schema [" + databaseSchema + "]", e);
        }
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        final String generalSchema = domainService.getGeneralSchema();
        LOG.debug("Releasing connection, setting database schema to [{}] ", generalSchema);
        setSchema(connection, generalSchema);
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class aClass) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return null;
    }
}

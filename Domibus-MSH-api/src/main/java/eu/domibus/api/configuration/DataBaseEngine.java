package eu.domibus.api.configuration;


import org.apache.commons.lang3.StringUtils;

public enum DataBaseEngine {
    MYSQL,
    ORACLE,
    H2;

    public static DataBaseEngine getDatabaseEngine(final String property) {
        if (StringUtils.containsIgnoreCase(property, MYSQL.name())) {
            return DataBaseEngine.MYSQL;
        } else if (StringUtils.containsIgnoreCase(property, ORACLE.name())) {
            return DataBaseEngine.ORACLE;
        } else if (StringUtils.containsIgnoreCase(property, H2.name())) {
            return DataBaseEngine.H2;
        } else {
            throw new IllegalStateException("Unsupported database dialect:" + property);
        }

    }
}

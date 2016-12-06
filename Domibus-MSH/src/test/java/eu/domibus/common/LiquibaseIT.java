package eu.domibus.common;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class LiquibaseIT {

    private static final Log LOG = LogFactory.getLog(LiquibaseIT.class);

    private static final String MYSQL_DDL_PREFIX = "mysql5innoDb-";
    private static final String ORACLE_DDL_PREFIX = "oracle10g-";

    @Test
    public void checkPresenceOfLiquibaseXMLs() throws IOException, URISyntaxException {

        /*Verify that Domibus.properties file has been built*/
        if (null == getClass().getClassLoader().getResource("domibus.properties")) {
            LOG.error("This integration test is expected to run after mvn install. Expected domibus.properties from target folder was not found!");
            Assert.fail("This integration test is expected to run after mvn install. Expected domibus.properties from target folder was not found!");
        }
        /*Verify that Domibus.properties file has been built*/

        /*Find the Domibus artefact version number*/
        Properties prop = new Properties();
        prop.load(getClass().getClassLoader().getResourceAsStream("domibus.properties"));
        String domibusArtifactVersion = prop.getProperty("Artifact-Version");
        LOG.debug("domibusArtifactVersion:" + domibusArtifactVersion);
        domibusArtifactVersion = StringUtils.stripEnd(domibusArtifactVersion, "-SNAPSHOT");
        LOG.debug("domibusArtifactVersion after stripping snapshot:" + domibusArtifactVersion);
        /*Find the Domibus artefact version number*/

        /*Verify that SQL Scripts zip file has been built*/
        String sqlScriptsZipFilePath = "./target/domibus-MSH-3.3-SNAPSHOT-sql-scripts.zip";
        File sqlScriptsZipFile = new File(sqlScriptsZipFilePath);
        LOG.debug(sqlScriptsZipFile.getAbsolutePath());
        if (!sqlScriptsZipFile.exists() || !sqlScriptsZipFile.isFile()) {
            LOG.error("This integration test is expected to run after mvn install. Expected file:" + sqlScriptsZipFilePath + " was not found");
            Assert.fail("This integration test is expected to run after mvn install. Expected file:" + sqlScriptsZipFilePath + " was not found");
        }
        /*Verify that SQL Scripts zip file has been built*/


        boolean releaseMySQLFilePresent = false, releaseOracleFilePresent = false;
        boolean migrationMySQLFilePresent = false, migrationOracleFilePresent = false;
        ZipFile zipFile = new ZipFile(sqlScriptsZipFile);
        Enumeration zipFileEnumeration = zipFile.entries();
        while (zipFileEnumeration.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipFileEnumeration.nextElement();
            String zipEntryName = zipEntry.getName();

            if (!releaseMySQLFilePresent) {
                releaseMySQLFilePresent = (StringUtils.contains(zipEntryName, MYSQL_DDL_PREFIX) && StringUtils.contains(zipEntryName, domibusArtifactVersion) && StringUtils.endsWith(zipEntryName, ".ddl"));
                if (releaseMySQLFilePresent) {
                    LOG.debug("Found MySQL release DDL");
                }
            }

            if (!releaseOracleFilePresent) {
                releaseOracleFilePresent = (StringUtils.contains(zipEntryName, ORACLE_DDL_PREFIX) && StringUtils.contains(zipEntryName, domibusArtifactVersion) && StringUtils.endsWith(zipEntryName, ".ddl"));
                if (releaseOracleFilePresent) {
                    LOG.debug("Found Oracle release DDL");
                }
            }

            if (!migrationMySQLFilePresent) {
                migrationMySQLFilePresent = (StringUtils.contains(zipEntryName, MYSQL_DDL_PREFIX) && StringUtils.contains(zipEntryName, domibusArtifactVersion) && StringUtils.endsWith(zipEntryName, "-migration.ddl"));
                if (migrationMySQLFilePresent) {
                    LOG.debug("Found MySQL migration ddl for release:" + domibusArtifactVersion);
                }
            }
            if (!migrationOracleFilePresent) {
                migrationOracleFilePresent = (StringUtils.contains(zipEntryName, "oracle10g-") && StringUtils.contains(zipEntryName, domibusArtifactVersion) && StringUtils.endsWith(zipEntryName, "-migration.ddl"));
                if (migrationOracleFilePresent) {
                    LOG.debug("Found Oracle migration ddl for release:" + domibusArtifactVersion);
                }
            }
            if (releaseMySQLFilePresent && releaseOracleFilePresent && migrationMySQLFilePresent && migrationOracleFilePresent) {
                break;
            }
        }


        if (!releaseMySQLFilePresent) {
            LOG.error("The required MYSQL release DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersion);
            Assert.fail("The required MYSQL release DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersion);
        }

        if (!releaseOracleFilePresent) {
            LOG.error("The required ORACLE release DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersion);
            Assert.fail("The required ORACLE release DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersion);
        }

        if (!migrationMySQLFilePresent) {
            LOG.error("The required MYSQL migration DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersion);
            Assert.fail("The required MYSQL migration DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersion);
        }

        if (!migrationOracleFilePresent) {
            LOG.error("The required ORACLE migration DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersion);
            Assert.fail("The required ORACLE migration DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersion);
        }

    }
}

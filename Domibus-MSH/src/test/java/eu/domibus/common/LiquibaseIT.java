package eu.domibus.common;

import eu.domibus.common.util.DomibusPropertiesService;
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

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class LiquibaseIT {

    private static final Log LOG = LogFactory.getLog(LiquibaseIT.class);

    private static final String MYSQL_DDL_PREFIX = "mysql5innoDb-";
    private static final String ORACLE_DDL_PREFIX = "oracle10g-";
    private static final String MIGRATION_DDL_SUFFIX = "-migration.dll";

    @Test
    public void checkPresenceOfLiquibaseXMLs() throws IOException, URISyntaxException {


        String domibusArtifactVersion = retrieveDomibusArtifactVersion();
        String domibusArtifactVersionNoSnapshot = StringUtils.stripEnd(domibusArtifactVersion, "-SNAPSHOT");
        LOG.debug("domibusArtifactVersionNoSnapshot:" + domibusArtifactVersionNoSnapshot);

        File sqlScriptsZipFile = locateDomibusSqlScriptsFile(domibusArtifactVersion);





        /*boolean releaseMySQLFilePresent = false, releaseOracleFilePresent = false;
        boolean migrationMySQLFilePresent = false, migrationOracleFilePresent = false;
        ZipFile zipFile = new ZipFile(sqlScriptsZipFile);
        Enumeration zipFileEnumeration = zipFile.entries();
        while (zipFileEnumeration.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipFileEnumeration.nextElement();
            String zipEntryName = zipEntry.getName();

            if (!releaseMySQLFilePresent) {
                releaseMySQLFilePresent = (StringUtils.startsWith(zipEntryName, MYSQL_DDL_PREFIX) && StringUtils.contains(zipEntryName, domibusArtifactVersionNoSnapshot) && StringUtils.endsWith(zipEntryName, ".ddl"));
                if (releaseMySQLFilePresent) {
                    LOG.debug("Found MySQL release DDL");
                }
            }

            if (!releaseOracleFilePresent) {
                releaseOracleFilePresent = (StringUtils.startsWith(zipEntryName, ORACLE_DDL_PREFIX) && StringUtils.contains(zipEntryName, domibusArtifactVersionNoSnapshot) && StringUtils.endsWith(zipEntryName, ".ddl"));
                if (releaseOracleFilePresent) {
                    LOG.debug("Found Oracle release DDL");
                }
            }

            if (!migrationMySQLFilePresent) {
                migrationMySQLFilePresent = (StringUtils.startsWith(zipEntryName, MYSQL_DDL_PREFIX) && StringUtils.contains(zipEntryName, domibusArtifactVersionNoSnapshot) && StringUtils.endsWith(zipEntryName, MIGRATION_DDL_SUFFIX));
                if (migrationMySQLFilePresent) {
                    LOG.debug("Found MySQL migration ddl for release:" + domibusArtifactVersionNoSnapshot);
                }
            }
            if (!migrationOracleFilePresent) {
                migrationOracleFilePresent = (StringUtils.startsWith(zipEntryName, ORACLE_DDL_PREFIX) && StringUtils.contains(zipEntryName, domibusArtifactVersionNoSnapshot) && StringUtils.endsWith(zipEntryName, MIGRATION_DDL_SUFFIX));
                if (migrationOracleFilePresent) {
                    LOG.debug("Found Oracle migration ddl for release:" + domibusArtifactVersionNoSnapshot);
                }
            }
            if (releaseMySQLFilePresent && releaseOracleFilePresent && migrationMySQLFilePresent && migrationOracleFilePresent) {
                break;
            }
        }


        if (!releaseMySQLFilePresent) {
            LOG.error("The required MYSQL release DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersionNoSnapshot);
            Assert.fail("The required MYSQL release DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersionNoSnapshot);
        }

        if (!releaseOracleFilePresent) {
            LOG.error("The required ORACLE release DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersionNoSnapshot);
            Assert.fail("The required ORACLE release DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersionNoSnapshot);
        }

        if (!migrationMySQLFilePresent) {
            LOG.error("The required MYSQL migration DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersionNoSnapshot);
            Assert.fail("The required MYSQL migration DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersionNoSnapshot);
        }

        if (!migrationOracleFilePresent) {
            LOG.error("The required ORACLE migration DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersionNoSnapshot);
            Assert.fail("The required ORACLE migration DDL files from Liquibase has not been generated for current Domibus Snapshot version:" + domibusArtifactVersionNoSnapshot);
        }*/

    }

    private File locateDomibusSqlScriptsFile(String domibusArtifactVersion) {
        /*Verify that SQL Scripts zip file has been built*/
        String sqlScriptsZipFilePath = "/target/sql-scripts";
        File sqlScriptsZipFile = new File(sqlScriptsZipFilePath);
        LOG.debug("\n\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        LOG.debug("sqlScriptsZipFile.getAbsolutePath:" + sqlScriptsZipFile.getAbsolutePath());
        LOG.debug("sqlScriptsZipFile.exists:" + sqlScriptsZipFile.exists());
        LOG.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n\n\n");


        Assert.assertTrue("Check if file exists", sqlScriptsZipFile.exists());
        /*String sqlScriptsZipFilePath = "target/domibus-MSH-3.3-SNAPSHOT-sql-scripts.zip";
        File sqlScriptsZipFile = new File(sqlScriptsZipFilePath);
        LOG.debug(sqlScriptsZipFile.getAbsolutePath());
        if (!sqlScriptsZipFile.exists() || !sqlScriptsZipFile.isFile()) {
            LOG.error("This integration test is expected to run after mvn install. Expected file:" + sqlScriptsZipFilePath + " was not found");
            Assert.fail("This integration test is expected to run after mvn install. Expected file:" + sqlScriptsZipFilePath + " was not found");
        }*/
        /*Verify that SQL Scripts zip file has been built*/
        return sqlScriptsZipFile;
    }


    private String retrieveDomibusArtifactVersion() {
        DomibusPropertiesService domibusPropertiesService = new DomibusPropertiesService();
        String domibusArtifactVersion = domibusPropertiesService.getImplVersion();
        if (StringUtils.isBlank(domibusArtifactVersion)) {
            LOG.error("Domibus artefact version could not be loaded!!!");
            Assert.fail("Domibus artefact version could not be loaded!!!");
        }
        LOG.debug("Artefact Version loaded from the domibus.properties:" + domibusArtifactVersion);

        /*Verify that Domibus.properties file has been built*/
//        if (null == getClass().getClassLoader().getResource("domibus.properties")) {
//            LOG.error("This integration test is expected to run after mvn install. Expected domibus.properties from target folder was not found!");
//            Assert.fail("This integration test is expected to run after mvn install. Expected domibus.properties from target folder was not found!");
//        }
        /*Verify that Domibus.properties file has been built*/

        /*Find the Domibus artefact version number*/
//        Properties prop = new Properties();
//        prop.load(getClass().getClassLoader().getResourceAsStream("domibus.properties"));
//        String domibusArtifactVersion = prop.getProperty("Artifact-Version");
//        LOG.debug("domibusArtifactVersion:" + domibusArtifactVersion);
//        domibusArtifactVersion = StringUtils.stripEnd(domibusArtifactVersion, "-SNAPSHOT");
//        LOG.debug("domibusArtifactVersion after stripping snapshot:" + domibusArtifactVersion);
        /*Find the Domibus artefact version number*/
        return domibusArtifactVersion;
    }
}

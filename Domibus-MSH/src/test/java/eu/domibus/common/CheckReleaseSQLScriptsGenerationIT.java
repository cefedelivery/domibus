package eu.domibus.common;

import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Domibus uses Liquibase DB change management plugin to generate DDLs and migrations for various releases.
 * The plugin requires changelog xml files and corresponding changes in pom.xml for file generation.
 * This integration test is to verify that the necessary changelog files and pom.xml changes have been included.
 *
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class CheckReleaseSQLScriptsGenerationIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CheckReleaseSQLScriptsGenerationIT.class);

    private static final String MYSQL_DDL_PREFIX = "mysql5innoDb-";
    private static final String ORACLE_DDL_PREFIX = "oracle10g-";
    private static final String RELEASE_DDL_SUFFIX = ".ddl";
    private static final String MIGRATION_DDL_SUFFIX = "-migration.ddl";
    private static final String SQL_SCRIPTS_DIRECTORY_PATH = "../Domibus-MSH-db/target/sql-scripts";


    /**
     * This integration test fetches the current artefact version of Domibus and verifies that the generated
     * sql-scripts directory has the necessary MySQL and Oralce DDLs for the release.
     * <p>
     * This is expected to be executed on Integration-Test phase of maven build.
     * The required sql-scripts directory is generated during the maven generate-resources phase.
     * During install phase, the sql-scripts directory will be zipped into domibus-MSH-[ArtefactVersion]-sql-scripts.zip
     *
     * @throws IOException
     */
    @Test
    public void checkPresenceOfSQLScriptDDLsForRelease() throws IOException {

        String domibusArtifactVersion = retrieveDomibusArtifactVersion();
        File sqlScriptsDirectory = locateDomibusSqlScriptsDirectory();

        checkPresenceOfMySQLDDLs_ForRelease(domibusArtifactVersion, sqlScriptsDirectory);

        checkPresenceOfOracleDDLs_ForRelease(domibusArtifactVersion, sqlScriptsDirectory);
    }

    protected void checkPresenceOfOracleDDLs_ForRelease(String domibusArtifactVersion, File sqlScriptsDirectory) throws IOException {
        preVerifications(domibusArtifactVersion, sqlScriptsDirectory);

        String domibusArtifactVersionNoSnapshot = StringUtils.stripEnd(domibusArtifactVersion, "-SNAPSHOT");
        LOG.debug("domibusArtifactVersion_NoSnapshot:" + domibusArtifactVersionNoSnapshot);

        Assert.assertTrue("Oracle Release version DDLs should be present in " + sqlScriptsDirectory.getAbsolutePath(), checkPresenceOfFile(ORACLE_DDL_PREFIX, domibusArtifactVersionNoSnapshot, RELEASE_DDL_SUFFIX, sqlScriptsDirectory));
        Assert.assertTrue("Oracle Migration DDLs for release should be present in " + sqlScriptsDirectory.getAbsolutePath(), checkPresenceOfFile(ORACLE_DDL_PREFIX, domibusArtifactVersionNoSnapshot, MIGRATION_DDL_SUFFIX, sqlScriptsDirectory));
        if (!StringUtils.endsWith(domibusArtifactVersionNoSnapshot, ".0")) {
            Assert.assertFalse("Oracle Release DDLs should NOT end with .0 in " + sqlScriptsDirectory.getAbsolutePath(), checkPresenceOfFile(ORACLE_DDL_PREFIX, domibusArtifactVersionNoSnapshot + ".0", RELEASE_DDL_SUFFIX, sqlScriptsDirectory));
            Assert.assertFalse("Oracle Migration DDLs should NOT end with .0 in " + sqlScriptsDirectory.getAbsolutePath(), checkPresenceOfFile(ORACLE_DDL_PREFIX, domibusArtifactVersionNoSnapshot + ".0", MIGRATION_DDL_SUFFIX, sqlScriptsDirectory));
        }
    }

    protected void checkPresenceOfMySQLDDLs_ForRelease(String domibusArtifactVersion, File sqlScriptsDirectory) throws IOException {
        preVerifications(domibusArtifactVersion, sqlScriptsDirectory);

        String domibusArtifactVersionNoSnapshot = StringUtils.stripEnd(domibusArtifactVersion, "-SNAPSHOT");
        LOG.debug("domibusArtifactVersion_NoSnapshot:" + domibusArtifactVersionNoSnapshot);

        Assert.assertTrue("MySQL Release version DDLs should be present in " + sqlScriptsDirectory.getAbsolutePath(), checkPresenceOfFile(MYSQL_DDL_PREFIX, domibusArtifactVersionNoSnapshot, RELEASE_DDL_SUFFIX, sqlScriptsDirectory));
        Assert.assertTrue("MySQL Migration DDLs for release should be present in " + sqlScriptsDirectory.getAbsolutePath(), checkPresenceOfFile(MYSQL_DDL_PREFIX, domibusArtifactVersionNoSnapshot, MIGRATION_DDL_SUFFIX, sqlScriptsDirectory));
        if (!StringUtils.endsWith(domibusArtifactVersionNoSnapshot, ".0")) {
            Assert.assertFalse("MySQL Release DDLs should NOT end with .0 in " + sqlScriptsDirectory.getAbsolutePath(), checkPresenceOfFile(MYSQL_DDL_PREFIX, domibusArtifactVersionNoSnapshot + ".0", RELEASE_DDL_SUFFIX, sqlScriptsDirectory));
            Assert.assertFalse("MySQL Migration DDLs should NOT end with .0 in " + sqlScriptsDirectory.getAbsolutePath(), checkPresenceOfFile(MYSQL_DDL_PREFIX, domibusArtifactVersionNoSnapshot + ".0", MIGRATION_DDL_SUFFIX, sqlScriptsDirectory));
        }
    }

    protected void preVerifications(String domibusArtifactVersion, File sqlScriptsDirectory) {
        Assert.assertNotNull("Domibus Artefact Version should be initialized from properties file!", domibusArtifactVersion);
        Assert.assertNotNull(sqlScriptsDirectory);
        Assert.assertTrue("target/sql-scripts directory should be present!", sqlScriptsDirectory.isDirectory());
    }

    protected boolean checkPresenceOfFile(String prefixString, String artefactVersion, String suffixString, File sqlScriptsDirectory) throws IOException {
        boolean filePresentFlag = false;

        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sqlScriptsDirectory.toPath());
        for (Path entry : directoryStream) {
            String fileName = entry.getFileName().toString();
//            LOG.debug(fileName);
            if (StringUtils.startsWith(fileName, prefixString) && StringUtils.contains(fileName, artefactVersion) && StringUtils.endsWith(fileName, suffixString)) {
                filePresentFlag = true;
                LOG.debug("Located file with prefix: [" + prefixString + "] containing artefactVersion: [" + artefactVersion + "] and suffix string: [" + suffixString + "] in directory:" + sqlScriptsDirectory.getAbsolutePath());
                break;
            }
        }
        return filePresentFlag;
    }


    protected File locateDomibusSqlScriptsDirectory() {
        /*Verify that SQL Scripts zip Directory has been built*/
        /*The directory target/sql-scripts will be created during generate-resources phase.*/
        /*Only during install phase, will this folder be zipped to the SqlScripts-<ArtefactVersion>.zip*/
        File sqlScriptsDirectory = new File(SQL_SCRIPTS_DIRECTORY_PATH);
        LOG.debug("sqlScriptsDirectory.getAbsolutePath:" + sqlScriptsDirectory.getAbsolutePath());
        LOG.debug("sqlScriptsDirectory.exists:" + sqlScriptsDirectory.exists());

        Assert.assertTrue("Check if Directory exists", sqlScriptsDirectory.exists());
        Assert.assertTrue("Check if target/sql-scripts is a directory", sqlScriptsDirectory.isDirectory());
        return sqlScriptsDirectory;
    }


    protected String retrieveDomibusArtifactVersion() {
        /*During Maven compile phase the domibus.properties file in the target folder with the artefact version copied from the POM*/
        DomibusPropertiesService domibusPropertiesService = new DomibusPropertiesService();
        String domibusArtifactVersion = domibusPropertiesService.getImplVersion();
        if (StringUtils.isBlank(domibusArtifactVersion)) {
            LOG.error("Domibus artefact version could not be loaded!!!");
            Assert.fail("Domibus artefact version could not be loaded!!!");
        }
        LOG.debug("Artefact Version loaded from the domibus.properties:" + domibusArtifactVersion);

        return domibusArtifactVersion;
    }
}

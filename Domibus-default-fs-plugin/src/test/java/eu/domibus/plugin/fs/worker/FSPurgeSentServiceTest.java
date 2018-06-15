package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.FSPluginProperties;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@RunWith(JMockit.class)
public class FSPurgeSentServiceTest {

    @Tested
    private FSPurgeSentService instance;

    @Injectable
    private FSPluginProperties fsPluginProperties;

    @Injectable
    private FSFilesManager fsFilesManager;

    @Injectable
    private FSMultiTenancyService fsMultiTenancyService;

    @Injectable
    private DomibusConfigurationExtService domibusConfigurationExtService;

    private FileObject rootDir;
    private FileObject sentFolder;
    private FileObject oldFile;
    private FileObject recentFile;

    @Before
    public void setUp() throws IOException {
        String location = "ram:///FSPurgeSentServiceTest";

        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        sentFolder = rootDir.resolveFile(FSFilesManager.SENT_FOLDER);
        sentFolder.createFolder();

        oldFile = sentFolder.resolveFile("old_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.xml");
        oldFile.createFile();
        // set modified time to 30s ago
        oldFile.getContent().setLastModifiedTime(System.currentTimeMillis() - 30000);

        recentFile = sentFolder.resolveFile("recent_3c5558e4-7b6d-11e7-bb31-be2e44b06b36@domibus.eu.xml");
        recentFile.createFile();
    }

    @After
    public void tearDown() throws FileSystemException {
        rootDir.close();
        sentFolder.close();
    }

    @Test
    public void testPurgeMessages() throws FileSystemException, FSSetUpException {
        new Expectations(1, instance) {{
            fsPluginProperties.getDomains();
            result = Collections.emptyList();

            fsFilesManager.setUpFileSystem(null);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.SENT_FOLDER);
            result = sentFolder;

            fsFilesManager.findAllDescendantFiles(sentFolder);
            result = new FileObject[]{ recentFile, oldFile };

            fsPluginProperties.getSentPurgeExpired(null);
            result = 20;
        }};

        instance.purgeMessages();

        new VerificationsInOrder(1) {{
            fsFilesManager.deleteFile(oldFile);
        }};
    }

    @Test
    public void testPurgeMessages_Domain1_BadConfiguration() throws FileSystemException, FSSetUpException {
        new Expectations(1, instance) {{
            fsMultiTenancyService.verifyDomainExists("DOMAIN1");
            result = true;

            fsPluginProperties.getDomains();
            result = Collections.singletonList("DOMAIN1");

            fsFilesManager.setUpFileSystem("DOMAIN1");
            result = new FSSetUpException("Test-forced exception");
        }};

        instance.purgeMessages();

        new Verifications() {{
            fsFilesManager.deleteFile(withAny(oldFile));
            maxTimes = 0;
       }};
    }

}

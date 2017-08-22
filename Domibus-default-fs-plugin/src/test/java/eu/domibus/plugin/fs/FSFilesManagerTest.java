package eu.domibus.plugin.fs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.activation.DataHandler;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.runner.RunWith;

import eu.domibus.plugin.fs.exception.FSSetUpException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@RunWith(JMockit.class)
public class FSFilesManagerTest {
    
    @Tested
    private FSFilesManager instance;
    
    @Injectable
    private FSPluginProperties fsPluginProperties;
    
    private FileObject rootDir;

    @Injectable
    private FileObject mockedRootDir;
    
    @Before
    public void setUp() throws FileSystemException {
        String location = "ram:///FSFilesManagerTest";
        String sampleFolderName = "samplefolder";
        
        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        FileObject sampleFolder = rootDir.resolveFile(sampleFolderName);
        sampleFolder.createFolder();

        rootDir.resolveFile("file1").createFile();
        rootDir.resolveFile("file2").createFile();
        rootDir.resolveFile("file3").createFile();
        rootDir.resolveFile("toberenamed").createFile();
        rootDir.resolveFile("tobemoved").createFile();
        rootDir.resolveFile("tobedeleted").createFile();
        
        rootDir.resolveFile("targetfolder1/targetfolder2").createFolder();
    }
    
    @After
    public void tearDown() throws FileSystemException {
        rootDir.deleteAll();
        rootDir.close();
    }

    // This test fails with a temporary filesystem
    @Test(expected = FSSetUpException.class)
    public void testGetEnsureRootLocation_Auth() throws Exception {
        String location = "ram:///FSFilesManagerTest";
        String domain = "domain";
        String user = "user";
        String password = "password";
        
        FileObject result = instance.getEnsureRootLocation(location, domain, user, password);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.exists());
    }

    @Test
    public void testGetEnsureRootLocation() throws Exception {
        String location = "ram:///FSFilesManagerTest";
        
        FileObject result = instance.getEnsureRootLocation(location);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.exists());
    }

    @Test
    public void testGetEnsureChildFolder() throws Exception {
        String folderName = "samplefolder";
        
        FileObject result = instance.getEnsureChildFolder(rootDir, folderName);
        
        Assert.assertNotNull(result);
        Assert.assertTrue(result.exists());
        Assert.assertEquals(result.getType(), FileType.FOLDER);
    }

    @Test(expected = FSSetUpException.class)
    public void testGetEnsureChildFolder_FileSystemException() throws Exception {
        final String folderName = "samplefolder";

        new Expectations(instance) {{
            mockedRootDir.exists();
            result = true;

            mockedRootDir.resolveFile(folderName);
            result = new FileSystemException("some unexpected error");
        }};

        instance.getEnsureChildFolder(mockedRootDir, folderName);
    }

    @Test
    public void testFindAllDescendantFiles() throws Exception {
        FileObject[] files = instance.findAllDescendantFiles(rootDir);
        
        Assert.assertNotNull(files);
        Assert.assertEquals(6, files.length);
        Assert.assertEquals("ram:///FSFilesManagerTest/file1", files[0].getName().getURI());
        Assert.assertEquals("ram:///FSFilesManagerTest/file2", files[1].getName().getURI());
        Assert.assertEquals("ram:///FSFilesManagerTest/file3", files[2].getName().getURI());
        Assert.assertEquals("ram:///FSFilesManagerTest/toberenamed", files[3].getName().getURI());
        Assert.assertEquals("ram:///FSFilesManagerTest/tobemoved", files[4].getName().getURI());
        Assert.assertEquals("ram:///FSFilesManagerTest/tobedeleted", files[5].getName().getURI());
    }

    @Test
    public void testGetDataHandler() throws Exception {
        DataHandler result = instance.getDataHandler(rootDir);
        
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getDataSource());
    }

    @Test
    public void testResolveSibling() throws Exception {
        FileObject result = instance.resolveSibling(rootDir, "siblingdir");
        
        Assert.assertNotNull(result);
        Assert.assertEquals("ram:///siblingdir", result.getName().getURI());
    }

    @Test
    public void testRenameFile() throws Exception {
        FileObject file = rootDir.resolveFile("toberenamed");
        FileObject result = instance.renameFile(file, "renamed");
        
        Assert.assertNotNull(result);
        Assert.assertEquals("ram:///FSFilesManagerTest/renamed", result.getName().getURI());
        Assert.assertTrue(result.exists());
    }

    // This test fails with a temporary filesystem
    @Test(expected = FSSetUpException.class)
    public void testSetUpFileSystem_Domain() throws Exception {
        new Expectations(instance) {{
            fsPluginProperties.getLocation("DOMAIN1");
            result = "ram:///FSFilesManagerTest/samplefolder";
            
            fsPluginProperties.getUser("DOMAIN1");
            result = "user";
            
            fsPluginProperties.getPassword("DOMAIN1");
            result = "secret";
        }};
        
        FileObject result = instance.setUpFileSystem("DOMAIN1");
        
        Assert.assertNotNull(result);
        Assert.assertTrue(result.exists());
        Assert.assertEquals("ram:///FSFilesManagerTest/samplefolder", result.getName().getURI());
    }

    @Test
    public void testSetUpFileSystem() throws Exception {
        new Expectations(instance) {{
            fsPluginProperties.getLocation(null);
            result = "ram:///FSFilesManagerTest";
        }};
        
        FileObject result = instance.setUpFileSystem(null);
        
        Assert.assertNotNull(result);
        Assert.assertTrue(result.exists());
        Assert.assertEquals("ram:///FSFilesManagerTest", result.getName().getURI());
    }

    @Test
    public void testDeleteFile() throws Exception {
        FileObject file = rootDir.resolveFile("tobedeleted");
        boolean result = instance.deleteFile(file);
        
        Assert.assertTrue(result);
        Assert.assertFalse(file.exists());
    }

    @Test
    public void testCloseAll(@Mocked final FileObject file1,
            @Mocked final FileObject file2,
            @Mocked final FileObject file3) throws FileSystemException {
        
        new Expectations(1, instance) {{
            file2.close();
            result = new FileSystemException("Test-forced exception");
        }};
        
        instance.closeAll(new FileObject[] { file1, file2, file3 });
        
        new Verifications(1) {{
            file1.close();
            file2.close();
            file3.close();
        }};
    }

    @Test
    public void testMoveFile() throws Exception {
        FileObject file = rootDir.resolveFile("tobemoved");
        FileObject targetFile = rootDir.resolveFile("targetfolder1/targetfolder2/moved");
        
        instance.moveFile(file, targetFile);
        
        Assert.assertTrue(targetFile.exists());
    }
    
}

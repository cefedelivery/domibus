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
import mockit.Tested;
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
    
    public FSFilesManagerTest() {
    }
    
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
        rootDir.resolveFile("tobemoved").createFolder();
    }
    
    @After
    public void tearDown() throws FileSystemException {
        rootDir.close();
    }

    // TODO: find a way to make this test run with a temporary filesystem
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

    @Test
    public void testFindAllDescendantFiles() throws Exception {
        FileObject[] files = instance.findAllDescendantFiles(rootDir);
        
        Assert.assertNotNull(files);
        Assert.assertEquals(3, files.length);
        Assert.assertEquals("ram:///FSFilesManagerTest/file1", files[0].getName().getURI());
        Assert.assertEquals("ram:///FSFilesManagerTest/file2", files[1].getName().getURI());
        Assert.assertEquals("ram:///FSFilesManagerTest/file3", files[2].getName().getURI());
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
        FileObject file = rootDir.resolveFile("tobemoved");
        FileObject result = instance.renameFile(file, "moved");
        
        Assert.assertNotNull(result);
        Assert.assertEquals("ram:///FSFilesManagerTest/moved", result.getName().getURI());
        Assert.assertTrue(result.exists());
    }

    // TODO: find a way to make this test run with a temporary filesystem
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
    
}

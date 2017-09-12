package eu.domibus.plugin.fs.vfs.smb;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class SmbFileNameTest {
    
    private SmbFileName fileName;
    
    @Before
    public void setUp() throws FileSystemException {
        // smb://domain\\user:password@example.org:12345/sharename/file1
        fileName = new SmbFileName("smb", "example.org", 12345, "user", "password", "domain", "sharename", "/file1", FileType.FILE);
    }

    @Test
    public void testGetShare() throws FileSystemException {
        String result = fileName.getShare();
        
        Assert.assertEquals("sharename", result);
    }
    
    @Test
    public void testGetDomain() throws FileSystemException {
        String result = fileName.getDomain();
        
        Assert.assertEquals("domain", result);
    }

    @Test
    public void testCreateName() {
        FileName result = fileName.createName("/file2", FileType.FILE);
        
        Assert.assertEquals("smb://domain\\user:password@example.org:12345/sharename/file2", result.getURI());
    }

    @Test
    public void testGetUriWithoutAuth() throws Exception {
        // Exercise internal cache with two calls
        String result1 = fileName.getUriWithoutAuth();
        String result2 = fileName.getUriWithoutAuth();
        
        Assert.assertEquals("smb://example.org:12345/sharename/file1", result1);
        Assert.assertEquals("smb://example.org:12345/sharename/file1", result2);
        Assert.assertSame(result1, result2);
    }

    @Test
    public void testEquals_Equal() {
        SmbFileName fileName2 = new SmbFileName("smb", "example.org", 12345, "user", "password", "domain", "sharename", "/file1", FileType.FILE);
        
        boolean result1 = fileName.equals(fileName2);
        boolean result2 = fileName2.equals(fileName);
        
        Assert.assertTrue(result1);
        Assert.assertTrue(result2);
    }
    
    @Test
    public void testEquals_NotEqual() {
        SmbFileName fileName2 = new SmbFileName("smb", "example.org", 12345, "user", "password", "otherdomain", "sharename", "/file1", FileType.FILE);
        
        boolean result1 = fileName.equals(fileName2);
        boolean result2 = fileName2.equals(fileName);
        
        Assert.assertFalse(result1);
        Assert.assertFalse(result2);
    }

    @Test
    public void testHashCode() {
        SmbFileName fileName2 = new SmbFileName("smb", "example.org", 12345, "user", "password", "domain", "sharename", "/file1", FileType.FILE);
        
        int result1 = fileName.hashCode();
        int result2 = fileName2.hashCode();
        
        Assert.assertEquals(result1, result2);
    }
    
}

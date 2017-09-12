package eu.domibus.plugin.fs.vfs.smb;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class SmbFileNameParserTest {
    
    private SmbFileNameParser smbFileNameParser;

    @Before
    public void setUp() throws Exception {
        smbFileNameParser = new SmbFileNameParser();
    }

    @Test
    public void testGetInstance() {
        FileNameParser result1 = SmbFileNameParser.getInstance();
        FileNameParser result2 = SmbFileNameParser.getInstance();
        
        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);
        Assert.assertSame(result1, result2);
    }
    
    @Test
    public void testParseUri() throws FileSystemException {
        SmbFileName result = (SmbFileName) smbFileNameParser.parseUri(null, null, "smb://example.org/sharename/file1");
        
        Assert.assertNotNull(result);
        Assert.assertEquals("smb://example.org/sharename/file1", result.getURI());
    }

    @Test
    public void testParseUri_AllFields() throws FileSystemException {
        SmbFileName result = (SmbFileName) smbFileNameParser.parseUri(null, null, "smb://domain\\user:password@example.org:12345/sharename/file1");
        
        Assert.assertNotNull(result);
        Assert.assertEquals("smb://domain\\user:password@example.org:12345/sharename/file1", result.getURI());
    }

    @Test
    public void testParseUri_NoDomain() throws FileSystemException {
        SmbFileName result = (SmbFileName) smbFileNameParser.parseUri(null, null, "smb://user:password@example.org/sharename/file1");
        
        Assert.assertNotNull(result);
        Assert.assertEquals("smb://user:password@example.org/sharename/file1", result.getURI());
    }

    @Test(expected = FileSystemException.class)
    public void testParseUri_EmptyShareName() throws FileSystemException {
        SmbFileName result = (SmbFileName) smbFileNameParser.parseUri(null, null, "smb://example.org/");
    }

    @Test(expected = FileSystemException.class)
    public void testParseUri_NoShareName() throws FileSystemException {
        SmbFileName result = (SmbFileName) smbFileNameParser.parseUri(null, null, "smb://example.org");
    }
    
}

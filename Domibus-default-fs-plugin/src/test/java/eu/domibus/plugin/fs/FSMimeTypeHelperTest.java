package eu.domibus.plugin.fs;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMimeTypeHelperTest {
    
    public FSMimeTypeHelperTest() {
    }

    @Test
    public void testGetMimeType_Text() {
        String result = FSMimeTypeHelper.getMimeType("file.txt");
        
        Assert.assertEquals("text/plain", result);
    }
    
    @Test
    public void testGetMimeType_Xml() {
        String result = FSMimeTypeHelper.getMimeType("file.xml");
        
        Assert.assertEquals("application/xml", result);
    }
    
    @Test
    public void testGetMimeType_Pdf() {
        String result = FSMimeTypeHelper.getMimeType("file.pdf");
        
        Assert.assertEquals("application/pdf", result);
    }

    @Test
    public void testGetExtension_Text() throws Exception {
        String result = FSMimeTypeHelper.getExtension("text/plain");
        
        Assert.assertEquals(".txt", result);
    }

    @Test
    public void testGetExtension_Xml() throws Exception {
        String result = FSMimeTypeHelper.getExtension("application/xml");
        
        Assert.assertEquals(".xml", result);
    }
    
    @Test
    public void testGetExtension_TextXml() throws Exception {
        String result = FSMimeTypeHelper.getExtension("text/xml");
        
        Assert.assertEquals(".xml", result);
    }

    @Test
    public void testGetExtension_Pdf() throws Exception {
        String result = FSMimeTypeHelper.getExtension("application/pdf");
        
        Assert.assertEquals(".pdf", result);
    }

    @Test
    public void testFixMimeType() {
        String result = FSMimeTypeHelper.fixMimeType("application/xml");
        
        Assert.assertEquals("text/xml", result);
    }
    
    @Test
    public void testFixMimeType_Passthrough() {
        String result = FSMimeTypeHelper.fixMimeType("text/plain");
        
        Assert.assertEquals("text/plain", result);
    }
    
}

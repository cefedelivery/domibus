package eu.domibus.plugin.fs;

import org.junit.Assert;
import org.junit.Test;

import eu.domibus.common.MessageStatus;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSFileNameHelperTest {
    
    public FSFileNameHelperTest() {
    }

    @Test
    public void testIsAnyState() {
        boolean result = FSFileNameHelper.isAnyState("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf.READY_TO_SEND");
        
        Assert.assertTrue(result);
    }
    
    @Test
    public void testIsAnyState_Fail() {
        boolean result = FSFileNameHelper.isAnyState("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf");
        
        Assert.assertFalse(result);
    }

    @Test
    public void testIsProcessed() {
        boolean result = FSFileNameHelper.isProcessed("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf");
        
        Assert.assertTrue(result);
    }
    
    @Test
    public void testIsProcessed_NoExtension() {
        boolean result = FSFileNameHelper.isProcessed("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");
        
        Assert.assertTrue(result);
    }

    @Test
    public void testIsProcessed_Fail1() {
        // missing one character in UUID in message ID
        boolean result = FSFileNameHelper.isProcessed("invoice_c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf");
        
        Assert.assertFalse(result);
    }

    @Test
    public void testIsProcessed_Fail2() {
        // missing underscore before UUID
        boolean result = FSFileNameHelper.isProcessed("invoice3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf");
        
        Assert.assertFalse(result);
    }

    @Test
    public void testIsProcessed_Fail3() {
        // missing message ID
        boolean result = FSFileNameHelper.isProcessed("invoice.pdf");
        
        Assert.assertFalse(result);
    }
    
    @Test
    public void testIsMessageRelated() {
        boolean result = FSFileNameHelper.isMessageRelated("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf", "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");
        
        Assert.assertTrue(result);
    }
    
    @Test
    public void testIsMessageRelated_Fail1() {
        // missing message ID
        boolean result = FSFileNameHelper.isMessageRelated("invoice.pdf", "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");
        
        Assert.assertFalse(result);
    }
    
    @Test
    public void testIsMessageRelated_Fail2() {
        // missing one character in UUID in message ID
        boolean result = FSFileNameHelper.isMessageRelated("invoice_c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf", "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");
        
        Assert.assertFalse(result);
    }
    
    @Test
    public void testIsMessageRelated_Fail3() {
        // missing underscore before UUID
        boolean result = FSFileNameHelper.isMessageRelated("invoice3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf", "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");
        
        Assert.assertFalse(result);
    }

    @Test
    public void testDeriveFileName() {
        String result = FSFileNameHelper.deriveFileName("invoice.pdf","3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");

        Assert.assertEquals("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf", result);
    }

    @Test
    public void testDeriveFileName_MultipleParts1() {
        String result = FSFileNameHelper.deriveFileName("invoice.foo.pdf","3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");

        Assert.assertEquals("invoice.foo_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf", result);
    }

    @Test
    public void testDeriveFileName_MultipleParts2() {
        String result = FSFileNameHelper.deriveFileName("invoice.foo.bar.pdf","3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");

        Assert.assertEquals("invoice.foo.bar_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf", result);
    }

    @Test
    public void testDeriveFileName_NoExtension() {
        String result = FSFileNameHelper.deriveFileName("invoice","3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu");

        Assert.assertEquals("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu", result);
    }

    @Test
    public void testDeriveFileName_NoStatus() {
        String result = FSFileNameHelper.deriveFileName("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf", MessageStatus.READY_TO_SEND);

        Assert.assertEquals("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf.READY_TO_SEND", result);
    }
    
    @Test
    public void testDeriveFileName_ReplaceStatus() {
        String result = FSFileNameHelper.deriveFileName("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf.READY_TO_SEND", MessageStatus.SEND_ENQUEUED);

        Assert.assertEquals("invoice_3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu.pdf.SEND_ENQUEUED", result);
    }
    
}

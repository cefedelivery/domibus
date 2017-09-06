package eu.domibus.common.exception;

import eu.domibus.common.ErrorCode;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by muellers on 4/20/16.
 */
public class EbMS3ExceptionTest {

    private EbMS3Exception ebMS3Exception;
    private static String ERROR_DETAIL_256CHARS = "OkgJvOG5Xp7rL1CzL5AXjdpgDGCYFIxXw43k6D87NA27CnnY3SKDX5FDGnU90IW6uNGMgxqi3nvbpMyIIxcuOLm9PP8cVytva0uyGiyiJHituKdj9bxnxYeRazfqLOz8HvfVfHxFF3JsXWwndiCgTUIdVzeDXnPt6tSB5NOEPdq6tbH7WScgY2kHl0VBhW8eGZu220D2MwSuFIFh6k2U2VzCd80eKz0bQlcOAQpDN2Pssj308uWULedijmPbvRoH";
    private static String ERROR_DETAIL_254CHARS = "gJvOG5Xp7rL1CzL5AXjdpgDGCYFIxXw43k6D87NA27CnnY3SKDX5FDGnU90IW6uNGMgxqi3nvbpMyIIxcuOLm9PP8cVytva0uyGiyiJHituKdj9bxnxYeRazfqLOz8HvfVfHxFF3JsXWwndiCgTUIdVzeDXnPt6tSB5NOEPdq6tbH7WScgY2kHl0VBhW8eGZu220D2MwSuFIFh6k2U2VzCd80eKz0bQlcOAQpDN2Pssj308uWULedijmPbvRoH";

    @Before
    public void setup() {
        ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, null, UUID.randomUUID().toString(), null);
    }

    @Test
    public void getErrorDetail_CharacterLimitReached() {
        ebMS3Exception.setErrorDetail(ERROR_DETAIL_256CHARS);
        assertTrue(ERROR_DETAIL_256CHARS.length() == 256);
        assertTrue(ebMS3Exception.getErrorDetail().length() == 255);
    }

    @Test
    public void getErrorDetail_CharacterLimitNotReached() {
        ebMS3Exception.setErrorDetail(ERROR_DETAIL_254CHARS);
        assertTrue(ebMS3Exception.getErrorDetail().length() == 254);
    }

    @Test
    public void getErrorDetail_Empty() {
        ebMS3Exception.setErrorDetail("");
        assertTrue(ebMS3Exception.getErrorDetail().length() == 0);
    }

    @Test
    public void getErrorDetail_Null() {
        ebMS3Exception.setErrorDetail(null);
        assertTrue(ebMS3Exception.getErrorDetail() == null);
    }

    @Test
    public void getFaultInfo_ErrorDetail_CharacterLimitReached() {
        ebMS3Exception.setErrorDetail(ERROR_DETAIL_256CHARS);
        assertEquals(ebMS3Exception.getErrorDetail().length(), ebMS3Exception.getFaultInfoError().getErrorDetail().length());
    }

}
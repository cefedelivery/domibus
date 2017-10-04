package eu.domibus.common.util;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.ErrorLogEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class ErrorLogEntryTruncateUtilTest {
    @Test
    public void truncate257CharacterLongMessageId() throws Exception {
        final String messageId = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-";
        final String transformedmessageId = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789";
        final String errorDetail = "error detail";

        ErrorLogEntry errorLogEntry = new ErrorLogEntry(null, messageId, ErrorCode.EBMS_0010, errorDetail);

        new ErrorLogEntryTruncateUtil().truncate(errorLogEntry);

        assertEquals(errorDetail, errorLogEntry.getErrorDetail());
        assertEquals(transformedmessageId, errorLogEntry.getMessageInErrorId());
    }

    @Test
    public void truncate257CharacterErrorDetail() throws Exception {
        final String errorDetail = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-";
        final String transformedErrorDetail = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789";
        final String messageID = "messageId";

        ErrorLogEntry errorLogEntry = new ErrorLogEntry(null, messageID, ErrorCode.EBMS_0010, errorDetail);

        new ErrorLogEntryTruncateUtil().truncate(errorLogEntry);

        assertEquals(transformedErrorDetail, errorLogEntry.getErrorDetail());
        assertEquals(messageID, errorLogEntry.getMessageInErrorId());
    }

    @Test
    public void truncate257CharacterSignalMessageId() throws Exception {
        final String signalMessageID = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-";
        final String transformedSignalMessageID = "-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789-01234567890123456789012345678901234567890123456789";
        final String messageID = "messageId";
        final String errorDetail = "error detail";

        EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0006, errorDetail, messageID, null);
        ebMS3Exception.setSignalMessageId(signalMessageID);

        ErrorLogEntry errorLogEntry = new ErrorLogEntry(ebMS3Exception);

        new ErrorLogEntryTruncateUtil().truncate(errorLogEntry);

        assertEquals(messageID, errorLogEntry.getMessageInErrorId());
        assertEquals(errorDetail, errorLogEntry.getErrorDetail());
        assertEquals(transformedSignalMessageID, errorLogEntry.getErrorSignalMessageId());
    }

}
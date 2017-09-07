package eu.domibus.common.util;

import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
public class ErrorLogEntryTruncateUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorLogEntryTruncateUtil.class);

    public void truncate(ErrorLogEntry errorLogEntry) {
        final String initialMessageId = errorLogEntry.getMessageInErrorId();
        final String transformedMessageId = transform(initialMessageId);
        if (checkStringChanged(initialMessageId, transformedMessageId)) {
            errorLogEntry.setMessageInErrorId(transformedMessageId);
        }

        final String initialSignalId = errorLogEntry.getErrorSignalMessageId();
        final String transformedSignalId = transform(initialSignalId);
        if (checkStringChanged(initialSignalId, transformedSignalId)) {
            errorLogEntry.setErrorSignalMessageId(transformedSignalId);
        }

        final String initialErrorDetail = errorLogEntry.getErrorDetail();
        final String transformedErrorDetail = transform(initialErrorDetail);
        if (checkStringChanged(initialErrorDetail, transformedErrorDetail)) {
            errorLogEntry.setErrorDetail(transformedErrorDetail);
        }
    }

    private boolean checkStringChanged(final String initialString, final String transformedString) {
        if (StringUtils.isNoneEmpty(initialString) && !initialString.equals(transformedString)) {
            LOG.warn(initialString + " was truncated to " + transformedString + " before saving the error log");
            return true;
        }
        return false;
    }

    protected String transform(String toTransform) {
        return StringUtils.left(toTransform, 255);
    }
}

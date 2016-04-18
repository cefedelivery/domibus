package eu.domibus.messaging;

import eu.domibus.common.ErrorCode;

/**
 * Created by kochc01 on 11.04.2016.
 */
public class DuplicateMessageException extends MessagingProcessingException {


    public DuplicateMessageException(String message) {
        super(message);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0004);
    }


}

/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.messaging;

import eu.domibus.common.ErrorCode;

/**
 * This is the base class for exceptions during message processing between plugin and MSH, like errors during validation or mapping.
 * It is not intended for errors between MSHs.
 *
 * @author Christian Koch, Stefan Mueller
 */
public class MessagingProcessingException extends Exception {

    private ErrorCode ebms3ErrorCode = ErrorCode.EBMS_0004; //init to "other"

    public MessagingProcessingException(String message) {
        super(message);
    }

    public MessagingProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MessagingProcessingException() {
    }

    public MessagingProcessingException(Throwable cause) {
        super(cause);
    }

    /**
     * @return a corresponding ebMS3 error code
     */
    public ErrorCode getEbms3ErrorCode() {
        return ebms3ErrorCode;
    }

    /**
     * @param ebms3ErrorCode a corresponding ebMS3 error code
     */
    public void setEbms3ErrorCode(ErrorCode ebms3ErrorCode) {
        this.ebms3ErrorCode = ebms3ErrorCode;
    }
}

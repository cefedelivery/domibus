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
 * This exception indicates that a message is not corresponding to its associated PMode and thus cannot be processed.
 *
 * @author Christian Koch, Stefan Mueller
 */
public class PModeMismatchException extends MessagingProcessingException {
    public PModeMismatchException() {
    }

    public PModeMismatchException(Throwable cause) {
        super(cause);
    }

    public PModeMismatchException(String message) {
        super(message);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0010);
    }

    public PModeMismatchException(String message, Throwable cause) {
        super(message, cause);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0010);
    }

    public PModeMismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0010);
    }
}

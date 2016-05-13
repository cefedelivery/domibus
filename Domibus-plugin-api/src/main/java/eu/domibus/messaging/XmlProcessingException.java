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
 * This exception is useful to transmit XML errors which can occur during XML parsing and or during XML validation.
 *
 * @author Federico Martini
 */
public class XmlProcessingException extends MessagingProcessingException {

    public XmlProcessingException(Throwable cause) {
        super(cause);
    }

    public XmlProcessingException(String message) {
        super(message);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0065);
    }

    public XmlProcessingException(String message, Throwable cause) {
        super(message, cause);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0065);
    }

}

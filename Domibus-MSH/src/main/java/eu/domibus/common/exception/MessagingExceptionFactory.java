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

package eu.domibus.common.exception;

import eu.domibus.common.ErrorCode;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.plugin.exception.TransformationException;

/**
 * TODO: add class description
 */
public class MessagingExceptionFactory {


    public static MessagingProcessingException transform(EbMS3Exception originalException) {


        ErrorCode errorCode = originalException.getErrorCodeObject();
        MessagingProcessingException messagingProcessingException;

        String message = ErrorCode.EbMS3ErrorCode.findErrorCodeBy(originalException.getErrorCodeObject().getErrorCodeName()).getShortDescription() + "\r detail: " + originalException.getErrorDetail();

        switch (errorCode) {
            case EBMS_0007:
                messagingProcessingException = new TransformationException(message, originalException);
                break;
            case EBMS_0001:
            case EBMS_0010:
                messagingProcessingException = new PModeMismatchException(message, originalException);
                break;
            default:
                messagingProcessingException = new MessagingProcessingException(message, originalException);
        }

        messagingProcessingException.setEbms3ErrorCode(errorCode);
        return messagingProcessingException;
    }

    public static MessagingProcessingException transform(Exception originalException, ErrorCode errorCode) {
        MessagingProcessingException messagingProcessingException = new MessagingProcessingException(originalException.getMessage(), originalException);
        messagingProcessingException.setEbms3ErrorCode(errorCode);
        return messagingProcessingException;
    }

}

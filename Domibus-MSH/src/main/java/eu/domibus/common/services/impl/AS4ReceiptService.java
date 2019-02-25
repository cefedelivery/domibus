package eu.domibus.common.services.impl;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.ebms3.common.model.Messaging;

import javax.xml.soap.SOAPMessage;

/**
 * Class responsible for generating AS4 receipts
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface AS4ReceiptService {

    /**
     * Generates AS4 receipt based on a SOAPMessage request
     *
     * @param request
     * @param messaging
     * @param replyPattern
     * @param nonRepudiation
     * @param duplicate
     * @param selfSendingFlag
     * @return
     * @throws EbMS3Exception
     */
    SOAPMessage generateReceipt(SOAPMessage request,
                                Messaging messaging,
                                ReplyPattern replyPattern,
                                Boolean nonRepudiation,
                                Boolean duplicate,
                                Boolean selfSendingFlag) throws EbMS3Exception;

    /**
     * Generates AS4 receipt based on an already received request
     *
     * @param messageId
     * @param nonRepudiation
     * @return
     * @throws EbMS3Exception
     */
    SOAPMessage generateReceipt(String messageId, final Boolean nonRepudiation) throws EbMS3Exception;
}

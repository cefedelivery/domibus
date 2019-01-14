package eu.domibus.common.services.impl;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Reliability;
import eu.domibus.ebms3.common.model.Messaging;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface AS4ReceiptService {

    SOAPMessage generateReceipt(SOAPMessage request,
                                Messaging messaging,
                                Reliability reliability,
                                Boolean nonRepudiation,
                                Boolean duplicate,
                                boolean selfSendingFlag) throws EbMS3Exception;
}

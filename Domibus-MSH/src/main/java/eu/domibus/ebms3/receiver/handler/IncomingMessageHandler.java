package eu.domibus.ebms3.receiver.handler;

import eu.domibus.ebms3.common.model.Messaging;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface IncomingMessageHandler {

    SOAPMessage processMessage(final SOAPMessage request, final Messaging messaging);
}

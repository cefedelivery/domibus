package eu.domibus.plugin.transformer;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface OutgoingMessageTransformer {

    void transformOutgoingMessage(SOAPMessage message);
}

package eu.domibus.plugin.transformer;

import eu.domibus.plugin.Submission;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface OutgoingMessageTransformer {

    void transformOutgoingMessage(Submission submission, SOAPMessage message);
}

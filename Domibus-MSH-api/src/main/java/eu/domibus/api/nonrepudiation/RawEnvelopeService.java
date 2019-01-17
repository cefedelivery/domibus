package eu.domibus.api.nonrepudiation;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface RawEnvelopeService {

    String getRawXmlByMessageId(String messageId);
}

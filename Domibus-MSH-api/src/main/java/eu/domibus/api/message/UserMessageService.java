package eu.domibus.api.message;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
//TODO create a model agnostic of peristence/JAXB annotations for exposing the UserMessage details
public interface UserMessageService {

    String getFinalRecipient(final String messageId);
}

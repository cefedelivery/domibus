package eu.domibus.ext.services;

import eu.domibus.ext.exceptions.MessageExtException;

/**
 *  An interface containing utility operations for processing messages, message identifiers or any other message properties.
 *
 * @author Sebastian-Ion TINCU
 */
public interface MessageExtService {

    /**
     * Returns a clean message identifier free from any whitespace and control character present at its beginning or its end.
     *
     * @param messageId the message identifier to clean up
     * @return The message identifier with all the whitespace and control characters removed from its beginning and its end
     * or the empty string {@code ""} if the message identifier is only made up of these characters.
     * @throws MessageExtException when something wrong occurs while trying to clean the message identifier
     */
    String cleanMessageIdentifier(String messageId) throws MessageExtException;

}

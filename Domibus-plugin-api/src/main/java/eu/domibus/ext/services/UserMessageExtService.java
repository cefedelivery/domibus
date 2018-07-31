package eu.domibus.ext.services;

import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageExtException;

/**
 * Service used to to do some operation related with user messages.
 *
 * <p>Operations available in the {@link UserMessageExtService} : </p>
 * <ul>
 *     <li>Gets a User Message ({@link #getMessage(String)}</li>
 * </ul>
 *
 * @author Tiago Miguel
 * @since 4.0
 */
public interface UserMessageExtService {

    /**
     * Gets a user message
     *
     * @param messageId The message id of the message to be get
     * @return The user message {@link UserMessageDTO}
     * @throws UserMessageExtException Raised in case an exception occurs while trying to get the user message {@link UserMessageExtException}
     */
    UserMessageDTO getMessage(String messageId) throws UserMessageExtException;
}

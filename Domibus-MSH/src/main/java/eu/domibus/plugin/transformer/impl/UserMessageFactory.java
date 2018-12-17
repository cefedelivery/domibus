package eu.domibus.plugin.transformer.impl;

import eu.domibus.core.message.fragment.MessageGroupEntity;
import eu.domibus.ebms3.common.model.UserMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface UserMessageFactory {

    UserMessage createUserMessageFragment(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, int fragmentNumber, String fragmentFile);

}

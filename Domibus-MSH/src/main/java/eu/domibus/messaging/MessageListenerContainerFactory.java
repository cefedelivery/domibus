package eu.domibus.messaging;

import eu.domibus.api.multitenancy.Domain;
import org.springframework.jms.listener.MessageListenerContainer;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface MessageListenerContainerFactory {

    MessageListenerContainer createSendMessageListenerContainer(Domain domain);

    MessageListenerContainer createSendLargeMessageListenerContainer(Domain domain);

    MessageListenerContainer createSplitAndJoinListenerContainer(Domain domain);

    MessageListenerContainer createPullReceiptListenerContainer(Domain domain);

    MessageListenerContainer createRetentionListenerContainer(Domain domain);
}

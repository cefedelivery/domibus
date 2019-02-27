package eu.domibus.messaging;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ebms3.sender.LargeMessageSenderListener;
import eu.domibus.ebms3.sender.MessageSenderListener;
import eu.domibus.ebms3.sender.SplitAndJoinListener;
import eu.domibus.core.pull.PullReceiptListener;
import eu.domibus.ebms3.sender.MessageSender;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Configuration
public class MessageListenerContainerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListenerContainerConfiguration.class);
    public static final String PROPERTY_LARGE_FILES_CONCURRENCY = "domibus.dispatcher.largeFiles.concurrency";
    public static final String PROPERTY_SPLIT_AND_JOIN_CONCURRENCY = "domibus.dispatcher.splitAndJoin.concurrency";
    private static final String DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY = "domibus.pull.receipt.queue.concurrency";

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue sendMessageQueue;

    @Autowired
    @Qualifier("sendPullReceiptQueue")
    private Queue sendPullReceiptQueue;

    @Autowired
    @Qualifier("sendLargeMessageQueue")
    private Queue sendLargeMessageQueue;

    @Autowired
    @Qualifier("splitAndJoinQueue")
    private Queue splitAndJoinQueue;

    @Autowired
    @Qualifier("messageSenderListener")
    private MessageSenderListener messageSenderListener;

    @Autowired
    @Qualifier("largeMessageSenderListener")
    private LargeMessageSenderListener largeMessageSenderListener;

    @Autowired
    @Qualifier("splitAndJoinListener")
    private SplitAndJoinListener splitAndJoinListener;

    @Autowired
    @Qualifier("pullReceiptListener")
    private PullReceiptListener pullReceiptListener;

    @Autowired
    @Qualifier("domibusJMS-XAConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Bean(name = "dispatchContainer")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultMessageListenerContainer createSendMessageListener(Domain domain) {
        LOG.debug("Instantiating the DefaultMessageListenerContainer for domain [{}]", domain);
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

        messageListenerContainer.setMessageSelector(MessageConstants.DOMAIN + "='" + domain.getCode() + "'");

        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(sendMessageQueue);
        messageListenerContainer.setMessageListener(messageSenderListener);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(domibusPropertyProvider.getDomainProperty(domain,"domibus.dispatcher.concurency"));
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        messageListenerContainer.afterPropertiesSet();

        return messageListenerContainer;
    }

    /**
     * Creates the large message JMS listener(domain dependent)
     * @param domain
     * @return
     */
    @Bean(name = "sendLargeMessageContainer")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultMessageListenerContainer createSendLargeMessageListener(Domain domain) {
        LOG.debug("Instantiating the createSendLargeMessageListenerContainer for domain [{}]", domain);
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

        messageListenerContainer.setMessageSelector(MessageConstants.DOMAIN + "='" + domain.getCode() + "'");

        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(sendLargeMessageQueue);
        messageListenerContainer.setMessageListener(largeMessageSenderListener);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(domibusPropertyProvider.getDomainProperty(domain, PROPERTY_LARGE_FILES_CONCURRENCY));
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        messageListenerContainer.afterPropertiesSet();

        return messageListenerContainer;
    }

    /**
     * Creates the SplitAndJoin JMS listener(domain dependent)
     * @param domain
     * @return
     */
    @Bean(name = "splitAndJoinContainer")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultMessageListenerContainer createSplitAndJoinListener(Domain domain) {
        LOG.debug("Instantiating the createSplitAndJoinListener for domain [{}]", domain);
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

        messageListenerContainer.setMessageSelector(MessageConstants.DOMAIN + "='" + domain.getCode() + "'");

        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(splitAndJoinQueue);
        messageListenerContainer.setMessageListener(splitAndJoinListener);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(domibusPropertyProvider.getDomainProperty(domain, PROPERTY_SPLIT_AND_JOIN_CONCURRENCY));
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        messageListenerContainer.afterPropertiesSet();

        return messageListenerContainer;
    }
    @Bean(name = "pullReceiptContainer")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultMessageListenerContainer createPullReceiptListener(Domain domain) {
        LOG.debug("Instantiating the createPullReceiptListener for domain [{}]", domain);
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

        messageListenerContainer.setMessageSelector(MessageConstants.DOMAIN + "='" + domain.getCode() + "'");

        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(sendPullReceiptQueue);
        messageListenerContainer.setMessageListener(pullReceiptListener);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY));
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        messageListenerContainer.afterPropertiesSet();

        return messageListenerContainer;
    }

}

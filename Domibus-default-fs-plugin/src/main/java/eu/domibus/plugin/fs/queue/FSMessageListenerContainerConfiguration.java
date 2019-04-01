package eu.domibus.plugin.fs.queue;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.fs.FSPluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

/**
 * Configuration class for JMS queues used in FS Plugin
 * <p>
 * It will contain all configuration for all defined queues
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Configuration
public class FSMessageListenerContainerConfiguration {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSMessageListenerContainerConfiguration.class);

    @Autowired
    @Qualifier("fsPluginSendQueue")
    private Queue fsPluginSendQueue;

    @Qualifier("fsSendMessageListener")
    @Autowired
    private FSSendMessageListener fsSendMessageListener;

    @Autowired
    @Qualifier("domibusJMS-XAConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    private FSPluginProperties fsPluginProperties;

    @Bean(name = "fsPluginOutContainer")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public MessageListenerContainer createDefaultMessageListenerContainer(DomainDTO domain) {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

        final String messageSelector = MessageConstants.DOMAIN + "='" + domain.getCode() + "'";
        final String queueConcurrency = fsPluginProperties.getMessageOutQueueConcurrency(domain.getCode());
        LOG.debug("fsPluginSendQueue concurrency set to: {}", queueConcurrency);

        messageListenerContainer.setMessageSelector(messageSelector);
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(fsPluginSendQueue);
        messageListenerContainer.setMessageListener(fsSendMessageListener);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(queueConcurrency);
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        messageListenerContainer.afterPropertiesSet();

        return messageListenerContainer;
    }
}

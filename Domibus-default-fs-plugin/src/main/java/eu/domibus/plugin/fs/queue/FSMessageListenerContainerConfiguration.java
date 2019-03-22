package eu.domibus.plugin.fs.queue;

import eu.domibus.ext.domain.DomainDTO;
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
 * Confgiyraiton class for JMS MessageListenerConfiguration
 */
@Configuration
public class FSMessageListenerContainerConfiguration {



    @Autowired
    @Qualifier("fsPluginOutQueue")
    private Queue fsPluginOutQueue;

    @Qualifier("fsOutMessageListener")
    @Autowired
    private FSOutMessageListener fsOutMessageListener;

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
        messageListenerContainer.setMessageSelector(messageSelector);

        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(fsPluginOutQueue);
        messageListenerContainer.setMessageListener(fsOutMessageListener);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(fsPluginProperties.getMessageOutQueueConcurrency(domain.getCode()));
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        messageListenerContainer.afterPropertiesSet();

        return messageListenerContainer;
    }
}

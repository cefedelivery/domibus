package eu.domibus.messaging;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.crypto.DomainCryptoServiceImpl;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.ebms3.sender.MessageSender;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Configuration
@DependsOn("springContextProvider")
public class MessageListenerContainerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListenerContainerConfiguration.class);

//    @Autowired
//    protected ApplicationContext applicationContext;

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue sendMessageQueue;

    @Autowired
    @Qualifier("messageSenderService")
    private MessageSender messageSenderService;

    @Autowired
    @Qualifier("domibusJMS-XAConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultMessageListenerContainer messageListenerContainer(Domain domain) {
        LOG.debug("Instantiating the DefaultMessageListenerContainer for domain [{}]", domain);
        return create(domain);
        //return getOrCreateMessageListenerContainer(domain);
    }

    DefaultMessageListenerContainer create(Domain domain) {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

        if(domain != null) {
            messageListenerContainer.setMessageSelector(MessageConstants.DOMAIN + "='" + domain.getCode() + "'");
        }

        //ConnectionFactory cf = (ConnectionFactory)applicationContext.getBean("domibusJMS-XAConnectionFactory");

        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(sendMessageQueue);
        messageListenerContainer.setMessageListener(messageSenderService);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(domibusPropertyProvider.getDomainProperty("domibus.dispatcher.concurency"));
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        return messageListenerContainer;
    }

    //    protected volatile Map<Domain, DefaultMessageListenerContainer> domainMessageListenerContainerProviderMap = new HashMap<>();


//    protected DefaultMessageListenerContainer getOrCreateMessageListenerContainer(Domain domain) {
//        //LOG.debug("Get domain CertificateProvider for domain [{}]", domain);
//        DefaultMessageListenerContainer messageListenerContainer = domainMessageListenerContainerProviderMap.get(domain);
//        if (messageListenerContainer == null) {
//            synchronized (domainMessageListenerContainerProviderMap) {
//                if (messageListenerContainer == null) {
//                    //LOG.debug("Creating domain messageListenerContainer for domain [{}]", domain);
//                    //messageListenerContainer = domainCertificateProviderFactory.createDomainCryptoService(domain);
//                    messageListenerContainer = create(domain);
//                    domainMessageListenerContainerProviderMap.put(domain, messageListenerContainer);
//                }
//            }
//        }
//        return messageListenerContainer;
//    }
}

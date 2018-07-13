package eu.domibus.core.alerts.configuration;

import freemarker.cache.ClassTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import javax.jms.ConnectionFactory;

import static org.springframework.jms.support.converter.MessageType.TEXT;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
public class AlertContextConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(AlertContextConfiguration.class);


    @Bean
    public MappingJackson2MessageConverter jackson2MessageConverter() {
        final MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
        mappingJackson2MessageConverter.setTargetType(TEXT);
        mappingJackson2MessageConverter.setTypeIdPropertyName("_type");
        return mappingJackson2MessageConverter;
    }

    @Bean
    public JmsTemplate jsonJmsTemplate(@Qualifier("domibusJMS-XAConnectionFactory") ConnectionFactory connectionFactory,
                                       MappingJackson2MessageConverter jackson2MessageConverter) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setSessionAcknowledgeModeName("AUTO_ACKNOWLEDGE");
        jmsTemplate.setMessageConverter(jackson2MessageConverter);
        return jmsTemplate;
    }

    @Bean
    public FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean() {
        final FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean =
                new FreeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactoryBean.setPreTemplateLoaders(new ClassTemplateLoader(AlertContextConfiguration.class,"/templates"));
        return freeMarkerConfigurationFactoryBean;
    }

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        return new JavaMailSenderImpl();
    }


}

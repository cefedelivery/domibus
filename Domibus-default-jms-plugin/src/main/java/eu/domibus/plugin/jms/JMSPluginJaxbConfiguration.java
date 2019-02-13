package eu.domibus.plugin.jms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Configuration
public class JMSPluginJaxbConfiguration {

    @Bean(name = "jmsJaxbContext")
    public JAXBContext createJaxbContext() throws JAXBException {
        return JAXBContext.newInstance("eu.domibus.plugin.jms.xml");
    }
}

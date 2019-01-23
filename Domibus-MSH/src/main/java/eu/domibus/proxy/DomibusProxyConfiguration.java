package eu.domibus.proxy;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author idragusa
 * @since 4.1
 *
 * Configuration of DomibusProxy.
 * Domibus takes one proxy for all requests (and domains)
 */
@Configuration
public class DomibusProxyConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusProxyConfiguration.class);

    @Bean(name = "domibusProxy")
    public DomibusProxy domibusProxy() {
        return new DomibusProxy();
    }
}

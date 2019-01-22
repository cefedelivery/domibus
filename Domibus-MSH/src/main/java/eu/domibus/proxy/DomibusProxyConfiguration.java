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
 */
@Configuration
public class DomibusProxyConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusProxyConfiguration.class);

    @Bean(name = "domibusProxy")
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public DomibusProxy domibusProxy() {
        return new DomibusProxy();
    }
}

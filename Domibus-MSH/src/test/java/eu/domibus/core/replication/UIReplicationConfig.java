package eu.domibus.core.replication;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@EnableAspectJAutoProxy(proxyTargetClass = true)
@Configuration
public class UIReplicationConfig {


    private final static Logger LOG = DomibusLoggerFactory.getLogger(UIReplicationConfig.class);

    @Bean
    public UIMessageDaoImpl uiMessageDao() {
        return new UIMessageDaoImpl();
    }


}

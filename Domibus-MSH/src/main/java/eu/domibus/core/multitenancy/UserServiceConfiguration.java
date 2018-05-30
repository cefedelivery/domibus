package eu.domibus.core.multitenancy;

import eu.domibus.common.services.UserService;
import eu.domibus.common.services.impl.SuperUserManagementServiceImpl;
import eu.domibus.common.services.impl.UserManagementServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Configuration
public class UserServiceConfiguration {


    @Bean
    @Primary
    public UserService userManagementService() {
        return new UserManagementServiceImpl();
    }

    @Bean
    public UserService superUserManagementService() {
        return new SuperUserManagementServiceImpl();
    }


}

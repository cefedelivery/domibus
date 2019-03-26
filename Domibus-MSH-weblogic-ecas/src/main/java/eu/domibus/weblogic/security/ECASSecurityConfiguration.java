package eu.domibus.weblogic.security;

import eu.domibus.configuration.security.AbstractWebSecurityConfigurerAdapter;
import eu.domibus.configuration.security.SecurityExternalAuthProviderCondition;
import eu.domibus.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Spring security configuration file for ECAS
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Conditional(SecurityExternalAuthProviderCondition.class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class ECASSecurityConfiguration extends AbstractWebSecurityConfigurerAdapter {

    @Autowired
    ECASUserDetailsService ecasUserDetailsService;

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new ECASAuthenticationServiceImpl();
    }

    @Override
    public void configureWebSecurity(WebSecurity web) throws Exception {
        web
                .ignoring().antMatchers("/logout/**");
    }

    @Override
    public void configureHttpSecurity(HttpSecurity http) throws Exception {
        http
                .jee().authenticatedUserDetailsService(ecasUserDetailsService)
                .and()
                .sessionManagement().sessionFixation().none();

    }

    @Autowired
    @Override
    protected void configureAuthenticationManagerBuilder(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(ecasUserDetailsService);
    }


}

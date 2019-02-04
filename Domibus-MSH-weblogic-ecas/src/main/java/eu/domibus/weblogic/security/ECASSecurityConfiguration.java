package eu.domibus.weblogic.security;

import eu.domibus.api.security.AuthRole;
import eu.domibus.configuration.security.AbstractWebSecurityConfigurerAdapter;
import eu.domibus.configuration.security.SecurityExternalAuthProviderCondition;
import eu.domibus.security.AuthenticationService;
import eu.domibus.web.filter.SetDomainFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

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
    CsrfTokenRepository tokenRepository;

    @Autowired
    RequestMatcher csrfURLMatcher;

    @Autowired
    SetDomainFilter setDomainFilter;

    @Autowired
    Http403ForbiddenEntryPoint http403ForbiddenEntryPoint;

    @Autowired
    ECASUserDetailsService ecasUserDetailsService;

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new ECASAuthenticationServiceImpl();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring().antMatchers("/services/**")
                .and()
                .ignoring().antMatchers("/ext/**")
                .and()
                .ignoring().antMatchers("/logout/**");
    }

    @Override
    public void configureHttpSecurity(HttpSecurity http) throws Exception {
        http
                .csrf().csrfTokenRepository(tokenRepository).requireCsrfProtectionMatcher(csrfURLMatcher)
                .and()
                .authorizeRequests()
                .antMatchers("/", "/index.html",
                        "/rest/security/authentication",
                        "/rest/application/info",
                        "/rest/application/name",
                        "/rest/application/fourcornerenabled",
                        "/rest/application/extauthproviderenabled",
                        "/rest/application/multitenancy").permitAll()
                .antMatchers("/rest/application/domains").hasAnyAuthority(AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers(HttpMethod.PUT, "/rest/security/user/password").authenticated()
                .antMatchers(HttpMethod.PUT, "/rest/security/user/domain").hasAnyAuthority(AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/pmode/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/party/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/truststore/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/messagefilters/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/jms/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/user/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/plugin/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/audit/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/alerts/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/testservice/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/logging/**").hasAnyAuthority(AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers("/rest/**").authenticated()
                .and()
                .jee().authenticatedUserDetailsService(ecasUserDetailsService)
                .and()
                .sessionManagement().sessionFixation().none()
                .and()
                .exceptionHandling().and()
                .headers().frameOptions().deny().contentTypeOptions().and().xssProtection().xssProtectionEnabled(true).and()
                .and()
                .httpBasic().authenticationEntryPoint(http403ForbiddenEntryPoint)
                .and()
                .addFilterBefore(setDomainFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Autowired
    @Override
    protected void configureAuthenticationManagerBuilder(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(ecasUserDetailsService);
    }


}

package eu.domibus.configuration.security;

import eu.domibus.common.services.impl.UserDetailServiceImpl;
import eu.domibus.security.AuthenticationService;
import eu.domibus.security.AuthenticationServiceImpl;
import eu.domibus.web.filter.SetDomainFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Default Spring security config for Domibus
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Conditional(SecurityInternalAuthProviderCondition.class)
@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    CsrfTokenRepository tokenRepository;

    @Autowired
    RequestMatcher csrfURLMatcher;

    @Autowired
    SetDomainFilter setDomainFilter;

    @Autowired
    Http403ForbiddenEntryPoint http403ForbiddenEntryPoint;

    @Autowired
    UserDetailServiceImpl userDetailService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().csrfTokenRepository(tokenRepository).requireCsrfProtectionMatcher(csrfURLMatcher)
                .and()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/rest/security/authentication").permitAll()
                .antMatchers("/rest/application/info").permitAll()
                .antMatchers("/rest/application/name").permitAll()
                .antMatchers("/rest/application/fourcornerenabled").permitAll()
                .antMatchers("/rest/application/multitenancy").permitAll()
                .antMatchers("/rest/application/domains").hasRole("AP_ADMIN")
                .antMatchers(HttpMethod.PUT, "/rest/security/user/password").hasAnyRole("USER", "ADMIN", "AP_ADMIN")
                .antMatchers(HttpMethod.PUT, "/rest/security/user/domain").hasAnyRole("AP_ADMIN")
                .antMatchers("/rest/pmode/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/party/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/truststore/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/messagefilters/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/jms/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/user/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/plugin/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/audit/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/alerts/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/testservice/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/logging/**").hasAnyRole("ADMIN", "AP_ADMIN")
                .antMatchers("/rest/**").hasAnyRole("USER", "ADMIN", "AP_ADMIN")
                .and()
                .exceptionHandling().and()
                .headers().frameOptions().deny().contentTypeOptions().and().xssProtection().xssProtectionEnabled(true).and()
                .and()
                .httpBasic().authenticationEntryPoint(http403ForbiddenEntryPoint)
                .and()
                .addFilterBefore(setDomainFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring().antMatchers("/services/**")
                .and()
                .ignoring().antMatchers("/ext/**")
                .and()
                .debug(true);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService);
    }

    @Bean(name = "authenticationManagerForAdminConsole")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl();
    }

}

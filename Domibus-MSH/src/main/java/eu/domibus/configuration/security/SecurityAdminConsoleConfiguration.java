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
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityAdminConsoleConfiguration extends AbstractWebSecurityConfigurerAdapter {

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

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean(name = "authenticationManagerForAdminConsole")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        return provider;
    }

    @Override
    public void configureHttpSecurity(HttpSecurity http) throws Exception {
        http
                .csrf().csrfTokenRepository(tokenRepository).requireCsrfProtectionMatcher(csrfURLMatcher)
                .and()
                .authorizeRequests()
                .antMatchers("/", "/index.html", "/login",
                        "/rest/security/authentication",
                        "/rest/application/info",
                        "/rest/application/name",
                        "/rest/application/fourcornerenabled",
                        "/rest/application/extauthproviderenabled",
                        "/rest/application/multitenancy",
                        "/rest/security/username").permitAll()
                .antMatchers("/rest/application/domains").hasRole(SUPER_ROLE)
                .antMatchers(HttpMethod.PUT, "/rest/security/user/password").authenticated()
                .antMatchers(HttpMethod.PUT, "/rest/security/user/domain").hasAnyRole(SUPER_ROLE)
                .antMatchers(HttpMethod.GET, "/rest/security/user").authenticated()
                .antMatchers("/rest/pmode/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/party/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/truststore/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/messagefilters/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/jms/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/user/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/plugin/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/audit/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/alerts/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/testservice/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/logging/**").hasAnyRole(ADMIN_ROLES)
                .antMatchers("/rest/**").authenticated()
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
    protected void configureAuthenticationManagerBuilder(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

}

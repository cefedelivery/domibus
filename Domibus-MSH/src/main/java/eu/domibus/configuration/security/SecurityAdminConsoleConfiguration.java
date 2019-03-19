package eu.domibus.configuration.security;

import eu.domibus.api.security.AuthRole;
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
                        "/rest/application/**",
                        "/rest/security/username").permitAll()
                .antMatchers("/rest/application/domains").hasAnyAuthority(AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers(HttpMethod.PUT, "/rest/security/user/password").authenticated()
                .antMatchers(HttpMethod.PUT, "/rest/security/user/domain").hasAnyAuthority(AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers(HttpMethod.GET, "/rest/security/user").authenticated()
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

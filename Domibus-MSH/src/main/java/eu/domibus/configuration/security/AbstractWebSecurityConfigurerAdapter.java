package eu.domibus.configuration.security;

import eu.domibus.api.security.AuthRole;
import eu.domibus.web.filter.SetDomainFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.ws.rs.HttpMethod;

/**
 * Abstract class for Domibus security configuration
 *
 * It extends {@link WebSecurityConfigurerAdapter} class and declares abstract methods
 * which need to be overridden by each implementation. Common code is exposed in non abstract methods.
 *
 * @author Catalin Enache
 * @since 4.1
 */
public abstract class AbstractWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
    @Autowired
    CsrfTokenRepository tokenRepository;

    @Autowired
    RequestMatcher csrfURLMatcher;

    @Autowired
    SetDomainFilter setDomainFilter;

    @Autowired
    Http403ForbiddenEntryPoint http403ForbiddenEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureHttpSecurityCommon(http);
        configureHttpSecurity(http);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        configureWebSecurityCommon(web);
        configureWebSecurity(web);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        configureAuthenticationManagerBuilder(auth);
    }


    /**
     * configure {@link HttpSecurity} - to be implemented
     *
     * @param http
     * @throws Exception
     */
    protected abstract void configureHttpSecurity(HttpSecurity http) throws Exception;

    /**
     * configure {@link WebSecurity} - to be implemented
     *
     * @param web
     * @throws Exception
     */
    protected abstract void configureWebSecurity(WebSecurity web) throws Exception;

    /**
     * configure {@link AuthenticationManagerBuilder} to be implemented
     * @param auth an {@link AuthenticationManagerBuilder}
     */
    protected abstract void configureAuthenticationManagerBuilder(AuthenticationManagerBuilder auth) throws  Exception;


    /**
     * common web security common configuration
     * @param web {@link WebSecurity} to configure
     */
    private void configureWebSecurityCommon(WebSecurity web) {
        web
                .ignoring().antMatchers("/services/**")
                .and()
                .ignoring().antMatchers("/ext/**");
    }

    /**
     * common http security config
     *
     * @param httpSecurity
     */
    private void configureHttpSecurityCommon(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
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
                        "/rest/application/supportteam",
                        "/rest/security/username").permitAll()
                .antMatchers("/rest/application/domains").hasAnyAuthority(AuthRole.ROLE_AP_ADMIN.name())
                .antMatchers(HttpMethod.PUT, "/rest/security/user/password").authenticated()
                .antMatchers( "/rest/security/user/domain").hasAnyAuthority(AuthRole.ROLE_USER.name(), AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
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
}

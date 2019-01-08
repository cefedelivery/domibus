package eu.domibus.security.ecas;

import eu.domibus.web.filter.SetDomainFilter;
import eu.domibus.web.matcher.URLCsrfMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@Configuration
@EnableWebSecurity(debug = true)
public class ECASSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ECASUserDetailsService ecasUserDetailsService;

    @Bean
    CsrfTokenRepository tokenRepository(){
        CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
        csrfTokenRepository.setCookieHttpOnly(false);
        return csrfTokenRepository;
    }

    @Bean
    RequestMatcher csrfURLMatcher() {
        URLCsrfMatcher requestMatcher = new URLCsrfMatcher();
        requestMatcher.setIgnoreUrl("/rest/security/authentication");
        return requestMatcher;
    }

    @Bean
    SetDomainFilter setDomainFilter() {
        return new SetDomainFilter();
    }

    @Bean
    Http403ForbiddenEntryPoint http403ForbiddenEntryPoint() {
        return new Http403ForbiddenEntryPoint();
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().csrfTokenRepository(tokenRepository()).requireCsrfProtectionMatcher(csrfURLMatcher())
                .and()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/rest/security/authentication").permitAll()
                .antMatchers("/rest/application/info").permitAll()
                .antMatchers("/rest/application/name").permitAll()
                .antMatchers("/rest/application/fourcornerenabled").permitAll()
                .antMatchers("/rest/application/multitenancy").permitAll()
                .antMatchers("/rest/application/domains").hasRole("AP_ADMIN")
                .antMatchers(HttpMethod.PUT,"/rest/security/user/password").hasAnyRole ("USER","ADMIN","AP_ADMIN")
                .antMatchers(HttpMethod.PUT,"/rest/security/user/domain").hasAnyRole ("AP_ADMIN")
                .antMatchers("/rest/pmode/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/party/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/truststore/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/messagefilters/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/jms/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/user/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/plugin/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/audit/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/alerts/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/testservice/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/logging/**").hasAnyRole ("ADMIN","AP_ADMIN")
                .antMatchers("/rest/**").hasAnyRole ("USER","ADMIN","AP_ADMIN")
                .and()
                .jee().authenticatedUserDetailsService(ecasUserDetailsService).and()
                .sessionManagement().sessionFixation().none().and()
                .exceptionHandling().and()
                .headers().frameOptions().deny().contentTypeOptions().and().xssProtection().xssProtectionEnabled(true).and()
                .and()
                .httpBasic().authenticationEntryPoint(http403ForbiddenEntryPoint())
                .and()
                .addFilterBefore(setDomainFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(ecasUserDetailsService);
    }

}

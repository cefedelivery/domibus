package eu.domibus.security.ecas;

import eu.domibus.web.matcher.URLCsrfMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
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

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/services/**");
    }

    @Bean
    CsrfTokenRepository getTokenRepository(){
        CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
        csrfTokenRepository.setCookieHttpOnly(false);
        return csrfTokenRepository;
    }

    @Bean
    RequestMatcher getCsrfURLMatcher() {
        URLCsrfMatcher requestMatcher = new URLCsrfMatcher();
        requestMatcher.setIgnoreUrl("/rest/security/authentication");
        return requestMatcher;
    }

//        <beans:bean id="csrfURLMatcher" class="eu.domibus.web.matcher.URLCsrfMatcher">
//        <beans:property name="ignoreUrl" value="/rest/security/authentication"/>
//    </beans:bean>

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().csrfTokenRepository(getTokenRepository()).requireCsrfProtectionMatcher(getCsrfURLMatcher())
                .and()
                .authorizeRequests()
                .antMatchers("/rest/user/**").hasAnyRole("ROLE_ADMIN","ROLE_AP_ADMIN")
                .anyRequest().permitAll()
                .and()
                .jee().authenticatedUserDetailsService(ecasUserDetailsService).and()
                .sessionManagement().sessionFixation().none().and()
                .exceptionHandling().and()
                .headers().frameOptions().sameOrigin().and();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(ecasUserDetailsService);
    }

}

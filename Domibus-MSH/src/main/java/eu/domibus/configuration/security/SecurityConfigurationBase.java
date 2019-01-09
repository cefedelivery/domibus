package eu.domibus.configuration.security;

import eu.domibus.web.filter.SetDomainFilter;
import eu.domibus.web.matcher.URLCsrfMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Security configuration which contains common beans to be instantiated
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Configuration
public class SecurityConfigurationBase {

    @Bean
    public CsrfTokenRepository tokenRepository(){
        CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
        csrfTokenRepository.setCookieHttpOnly(false);
        return csrfTokenRepository;
    }

    @Bean
    public RequestMatcher csrfURLMatcher() {
        URLCsrfMatcher requestMatcher = new URLCsrfMatcher();
        requestMatcher.setIgnoreUrl("/rest/security/authentication");
        return requestMatcher;
    }

    @Bean
    public SetDomainFilter setDomainFilter() {
        return new SetDomainFilter();
    }

    @Bean
    public Http403ForbiddenEntryPoint http403ForbiddenEntryPoint() {
        return new Http403ForbiddenEntryPoint();
    }

    @Bean
    public BCryptPasswordEncoder bcryptEncoder() {
        return new BCryptPasswordEncoder();
    }

}

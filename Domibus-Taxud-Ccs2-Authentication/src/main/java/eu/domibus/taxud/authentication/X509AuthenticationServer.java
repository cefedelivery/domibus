package eu.domibus.taxud.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@SpringBootApplication
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(value = "eu.domibus.taxud.authentication")
public class X509AuthenticationServer{



    public static void main(String[] args) {
        SpringApplication.run(X509AuthenticationServer.class, args);
    }
}

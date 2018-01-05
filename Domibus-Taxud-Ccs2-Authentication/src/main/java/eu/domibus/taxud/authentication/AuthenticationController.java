package eu.domibus.taxud.authentication;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
public class AuthenticationController {

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(value = "/authenticate")
    public String authenticate(){
        return "ok";

    }
}

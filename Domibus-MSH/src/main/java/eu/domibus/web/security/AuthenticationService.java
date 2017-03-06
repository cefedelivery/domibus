package eu.domibus.web.security;

import eu.domibus.web.rest.LoginDTO;
import eu.domibus.web.rest.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthenticationService {

    @Autowired
    @Qualifier("authenticationManagerForAdminConsole")
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    public UserDTO authenticate(LoginDTO loginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final User principal = (User) authentication.getPrincipal();

        //Parse Granted authorities to a list of string authorities
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : principal.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(loginDTO.getUsername());
        userDTO.setAuthorities(authorities);
        return userDTO;
    }
}

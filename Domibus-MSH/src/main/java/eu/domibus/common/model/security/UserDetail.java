package eu.domibus.common.model.security;

import com.google.common.collect.Sets;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserDetail implements UserDetails{
    private final User domibusUser;
    private final org.springframework.security.core.userdetails.User springUser;

    public UserDetail(final User user) {
        this.domibusUser=user;
        springUser=new org.springframework.security.core.userdetails.User(user.getUserName(),user.getPassword(),getGrantedAuthorities(Sets.newHashSet(user.getRoles())));
    }

    private Set<GrantedAuthority> getGrantedAuthorities(Set<UserRole> roles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (UserRole role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return springUser.getAuthorities();
    }

    @Override
    public String getPassword() {
        return springUser.getPassword();
    }

    @Override
    public String getUsername() {
        return springUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return springUser.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return springUser.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return springUser.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return springUser.isEnabled();
    }

    public String getMail(){
        return domibusUser.getEmail();
    }





}

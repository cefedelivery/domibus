package eu.domibus.common.model.security;

import com.google.common.collect.Lists;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserDetail implements UserDetails {
    private final UserDetails springUser;

    public UserDetail(final User user) {
        springUser = org.springframework.security.core.userdetails.User
                .withUsername(user.getUserName())
                .password(user.getPassword())
                .authorities(getGrantedAuthorities(user.getRoles()))
                .build();
    }

    private List<GrantedAuthority> getGrantedAuthorities(Collection<UserRole> roles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (UserRole role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return Lists.newArrayList(authorities);
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


}

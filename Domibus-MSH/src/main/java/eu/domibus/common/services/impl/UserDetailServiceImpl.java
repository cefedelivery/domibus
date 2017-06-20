package eu.domibus.common.services.impl;

import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
//@Service
//@Qualifier("userDetailService")
public class UserDetailServiceImpl implements UserService,UserDetailsService {


    @Autowired
    private UserDao userDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    /*@Override
    @Transactional(readOnly = true)
    public List<UserResponseRO> findUsers() {
        List<UserDetail> userDetails = getUserDetails();
        List<UserResponseRO> retval = new ArrayList<>();
        for (UserDetail userDetail : userDetails) {
            Collection<? extends GrantedAuthority> authorities = userDetail.getAuthorities();
            String concatAuthority = "";
            for (GrantedAuthority authority : authorities) {
                concatAuthority += authority.getAuthority();
            }
            retval.add(new UserResponseRO(userDetail.getUsername(), concatAuthority, userDetail.getMail(), userDetail.isEnabled()));
        }
        return retval;
    }

    public void addUser(UserRe)*/

    @Override
    @Transactional(readOnly = true)
    public  List<User> findUsers() {
        return userDao.listUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRole> findRoles() {
        return null;
    }

    @Override
    @Transactional
    public void createUser(User user){
            user.setPassword(bcryptEncoder.encode(user.getPassword()));
            userDao.create(user);
    }
    @Override
    @Transactional
    public void updateUser(User user){
        user.setPassword(bcryptEncoder.encode(user.getPassword()));
        userDao.create(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userDao.loadUserByUsername(userName);
        if(user==null){
            throw new UsernameNotFoundException(userName+" has not been found in system");
        }
        return new UserDetail(user);
    }
}

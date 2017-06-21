package eu.domibus.common.services.impl;

import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
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
public class UserDetailServiceImpl implements UserService{

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    private DomainExtConverter domainConverter;

    @Override
    @Transactional(readOnly = true)
    public  List<User> findUsers() {
        return userDao.listUsers();
    }


    @Override
    @Transactional
    public void saveUsers(List<eu.domibus.api.user.User> users) {
        for (eu.domibus.api.user.User user : users) {
            //@thom use enumeration
            if("NEW".equals(user.getStatus())){
                List<String> authorities = user.getAuthorities();
                User userEntity = domainConverter.convert(user, User.class);
                for (String authority : authorities) {
                    UserRole userRole = userRoleDao.findByName(authority);
                    userEntity.addRole(userRole);
                }
                userDao.create(userEntity);
            }
        }
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

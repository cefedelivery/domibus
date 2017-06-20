package eu.domibus.common.services.impl;

import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserDetailService;
import eu.domibus.web.rest.ro.UserResponseRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Service
public class UserDetailServiceImpl implements UserDetailService {


    @Autowired
    private UserDao userDao;

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
    public List<UserRole> findRoles() {
        return null;
    }
}

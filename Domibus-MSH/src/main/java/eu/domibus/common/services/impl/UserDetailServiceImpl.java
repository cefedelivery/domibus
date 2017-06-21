package eu.domibus.common.services.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserDetailServiceImpl implements UserService{

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDetailServiceImpl.class);

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
        Collection<eu.domibus.api.user.User> newUsers = filterNewUsers(users);
        LOG.info("New users:"+newUsers.size());
        insertNewUsers(newUsers);
        Collection<eu.domibus.api.user.User> noPasswordChangedModifiedUsers = filterModifiedWithoutPasswordChange(users);
        LOG.info("Modified users without password change:"+noPasswordChangedModifiedUsers.size());
    }

    private void insertNewUsers(Collection<eu.domibus.api.user.User> users) {
        for (eu.domibus.api.user.User user : users) {
            //@thom use enumeration
            if("NEW".equals(user.getStatus())){
                List<String> authorities = user.getAuthorities();
                User userEntity = domainConverter.convert(user, User.class);
                for (String authority : authorities) {
                    UserRole userRole = userRoleDao.findByName(authority);
                    userEntity.addRole(userRole);
                }
                userEntity.setPassword(bcryptEncoder.encode(userEntity.getPassword()));
                userDao.create(userEntity);
            }
        }
    }

    private Collection<eu.domibus.api.user.User> filterNewUsers(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, new Predicate<eu.domibus.api.user.User>() {
            @Override
            public boolean apply(eu.domibus.api.user.User user) {
                return "NEW".equals(user.getStatus());
            }
        });
    }
    private Collection<eu.domibus.api.user.User> filterModifiedWithoutPasswordChange(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, new Predicate<eu.domibus.api.user.User>() {
            @Override
            public boolean apply(eu.domibus.api.user.User user) {
                return "UPDATED".equals(user.getStatus()) && user.getPassword()==null;
            }
        });
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

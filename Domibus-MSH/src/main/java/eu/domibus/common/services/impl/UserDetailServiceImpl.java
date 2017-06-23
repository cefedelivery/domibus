package eu.domibus.common.services.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import eu.domibus.api.user.UserState;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserDetailServiceImpl implements UserDetailsService {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDetailServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    private DomainCoreConverter domainConverter;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userDao.loadActiveUserByUsername(userName);
        if (user == null) {
            throw new UsernameNotFoundException(userName + " has not been found in system");
        }
        return new UserDetail(user);
    }
}

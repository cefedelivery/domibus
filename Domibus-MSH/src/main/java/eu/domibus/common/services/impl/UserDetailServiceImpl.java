package eu.domibus.common.services.impl;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserDetailServiceImpl implements UserDetailsService {
    public static final String CHECK_DEFAULT_PASSWORD = "domibus.passwordPolicy.checkDefaultPassword";
    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDetailServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    @Transactional(readOnly = true, noRollbackFor = UsernameNotFoundException.class)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userDao.loadActiveUserByUsername(userName);
        if (user == null) {
            String msg = userName + " has not been found in system";
            LOG.warn(msg);
            throw new UsernameNotFoundException(msg);
        }

        UserDetail userDetail = new UserDetail(user);
        userDetail.setDefaultPasswordUsed(isDefaultPasswordUsed(user));
        userDetail.setDaysTillExpiration(userService.getDaysTillExpiration(userName));
        return userDetail;
    }

    private boolean isDefaultPasswordUsed(final User user ) {
        boolean checkDefaultPassword = Boolean.parseBoolean(domibusPropertyProvider.getProperty(CHECK_DEFAULT_PASSWORD));
        if (!checkDefaultPassword) {
            return false;
        }
        return user.hasDefaultPassword();
    }
}

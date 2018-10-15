package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.security.DefaultCredentials;
import org.apache.commons.lang3.StringUtils;
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

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDetailServiceImpl.class);

    protected static final String CHECK_DEFAULT_PASSWORD = "domibus.passwordPolicy.checkDefaultPassword";

    @Autowired
    private UserDao userDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(readOnly = true, noRollbackFor = UsernameNotFoundException.class)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userDao.loadActiveUserByUsername(userName);
        if (user == null) {
            String msg = userName + " has not been found in system";
            LOG.warn(msg);
            throw new UsernameNotFoundException(msg);
        }
        boolean defaultPasswordUsed = isDefaultPasswordUsed(userName, user.getPassword());
        UserDetail userDetail = new UserDetail(user, defaultPasswordUsed);

        userDetail.setDaysTillExpiration(userService.validateDaysTillExpiration(userName));
        return userDetail;
    }

    private boolean isDefaultPasswordUsed(final String user, final String password) {
        boolean checkDefaultPassword = Boolean.parseBoolean(domibusPropertyProvider.getOptionalDomainProperty(CHECK_DEFAULT_PASSWORD, "true"));
        if (!checkDefaultPassword) {
            return false;
        }
        boolean defaultPasswordUsed = false;
        String defaultPasswordForUser = DefaultCredentials.getDefaultPasswordForUser(user);
        if (defaultPasswordForUser != null) {
            defaultPasswordUsed = bcryptEncoder.matches(defaultPasswordForUser, password);
        }
        return defaultPasswordUsed;
    }

}

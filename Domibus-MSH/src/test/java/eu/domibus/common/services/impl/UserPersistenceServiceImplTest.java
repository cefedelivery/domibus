package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.core.converter.DomainCoreConverter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UserPersistenceServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    private UserRoleDao userRoleDao;

    @Injectable
    private BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private UserDomainService userDomainService;

    @Tested
    private UserPersistenceServiceImpl userPersistenceService;


    @Test
    public void prepareUserForUpdate() {
        final User userEntity = new User();
        userEntity.setActive(false);
        userEntity.setSuspensionDate(new Date());
        userEntity.setAttemptCount(5);
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setActive(true);
        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
        }};
        User user1 = userPersistenceService.prepareUserForUpdate(user);
        assertNull(user1.getSuspensionDate());
        assertEquals(0, user1.getAttemptCount(), 0d);
    }

}

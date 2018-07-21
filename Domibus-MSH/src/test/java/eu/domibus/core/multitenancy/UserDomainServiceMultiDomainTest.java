package eu.domibus.core.multitenancy;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import mockit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UserDomainServiceMultiDomainTest {

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected UserDomainDao userDomainDao;

    @Injectable
    protected UserDao userDao;

    @Injectable
    protected UserConverter userConverter;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Tested
    UserDomainServiceMultiDomainImpl userDomainServiceMultiDomainImpl;

    @Test
    public void testGetDomainForUser( ) {
        String user = "user1";
        String domain = "domain1";

        new Expectations() {{
            userDomainDao.findDomainByUser(user);
            result = domain;
        }};

        String dom = userDomainDao.findDomainByUser(user);

        new Expectations() {{
//            domainTaskExecutor.submit(() -> userDomainDao.findDomainByUser(user));
            domainTaskExecutor.submit((Callable<String>) any);
            result = dom;
        }};

        String result = userDomainServiceMultiDomainImpl.getDomainForUser(user);
        assertEquals(domain, result);
    }

    @Test
    public void testGetPreferredDomainForUser( ) {
        String user = "user1";
        String domain = "default";

        new Expectations() {{
            userDomainDao.findPreferredDomainByUser(user);
            result = domain;
        }};

        String dom = userDomainDao.findPreferredDomainByUser(user);

        new Expectations() {{
//            domainTaskExecutor.submit(() -> userDomainDao.findDomainByUser(user));
            domainTaskExecutor.submit((Callable<String>) any);
            result = dom;
        }};

        String result = userDomainServiceMultiDomainImpl.getPreferredDomainForUser(user);
        assertEquals(domain, result);
    }

    @Test
    public void testGetAllUserNames( ) {
        List<String> userNames = Arrays.asList("user1", "user2", "user3");

        new Expectations() {{
            userDomainDao.listAllUserNames();
            result = userNames;
        }};

        List<String> userNames2 = userDomainDao.listAllUserNames();

        new Expectations() {{
            domainTaskExecutor.submit((Callable<String>) any);
            result = userNames2;
        }};

        List<String> result = userDomainServiceMultiDomainImpl.getAllUserNames();
        assertEquals(userNames, result);
    }
}

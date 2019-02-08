package eu.domibus.core.multitenancy;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.UserDomain;
import eu.domibus.api.user.User;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.core.multitenancy.dao.UserDomainEntity;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UserDomainServiceMultiDomainTest {

    @Mock
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

    @Injectable
    protected DomibusCacheService domibusCacheService;

    @Injectable
    DomainCoreConverter domainCoreConverter;

    @Tested
    UserDomainServiceMultiDomainImpl userDomainServiceMultiDomainImpl;

    @Captor
    ArgumentCaptor argCaptor;

    @Test
    public void testGetDomainForUser() throws Exception {
        String user = "user1";
        String domain = "domain1";

        new Expectations() {{
            userDomainDao.findDomainByUser(user);
            result = domain;
        }};

        String mockResult = userDomainServiceMultiDomainImpl.getDomainForUser(user);
        String result = mockExecutorSubmit();

        assertEquals(result, domain);
    }

    @Test
    public void getAllUserDomainMappingsTest() throws Exception {
        List<UserDomainEntity> userDomainEntities = new ArrayList<>();
        List<UserDomain> userDomains = new ArrayList<>();

        new Expectations() {{
            userDomainDao.listAllUsers();
            result = userDomainEntities;
            domainCoreConverter.convert(userDomainEntities, UserDomain.class);
            result = userDomains;
        }};

        List<UserDomain> mockResult = userDomainServiceMultiDomainImpl.getAllUserDomainMappings();
        List<UserDomain> result = mockExecutorSubmit();

        new Verifications() {{
            userDomainDao.listAllUsers();
            times = 1;
            domainCoreConverter.convert((List<UserDomain>) any, UserDomain.class);
            times = 1;

            Assert.assertEquals(userDomains, result);
        }};
    }

    @Test
    public void testGetSuperUsers() throws Exception {
        eu.domibus.common.model.security.User userEntity = new eu.domibus.common.model.security.User();
        List<eu.domibus.common.model.security.User> userEntities = Arrays.asList(userEntity);
        User user = new User();
        List<User> users = Arrays.asList(user);

        new Expectations() {{
            userDao.listUsers();
            result = userEntities;
            userConverter.convert(userEntities);
            result = users;
        }};

        List<User> mockResult = userDomainServiceMultiDomainImpl.getSuperUsers();
        List<User> result = mockExecutorSubmit();

        Assert.assertEquals(users, result);
    }

    private <T> T mockExecutorSubmit() throws Exception {
        Mockito.verify(domainTaskExecutor).submit((Callable) argCaptor.capture());
        Callable<T> callable = (Callable<T>) argCaptor.getValue();
        return callable.call();
    }
}

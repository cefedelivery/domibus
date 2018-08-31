package eu.domibus.core.multitenancy;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import mockit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Injectable
    protected DomibusCacheService domibusCacheService;

    @Tested
    UserDomainServiceMultiDomainImpl userDomainServiceMultiDomainImpl;

    @Test
    public void testGetDomainForUser() {
        String user = "user1";
        String domain = "domain1";
        Callable<String> f;

        new Expectations() {{
            //domainTaskExecutor.submit(() -> userDomainDao.findDomainByUser(user));
            //result = userDomainDao.findDomainByUser(user);
            domainTaskExecutor.submit((Callable<String>) any);
            result = domain;

//            userDomainDao.findDomainByUser(user);
//            result = domain;
        }};

        String result = userDomainServiceMultiDomainImpl.getDomainForUser(user);
        assertEquals(result, domain);
    }


}

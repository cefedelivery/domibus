package eu.domibus.core.multitenancy;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void testGetDomainForUser() {
        String user = "user1";
        String domain = "domain1";
        //Callable<String> task = () -> userDomainDao.findDomainByUser(user);

        Expectations expectations = new Expectations() {{
            //domainTaskExecutor.submit(() -> userDomainDao.findDomainByUser((String) any));
            domainTaskExecutor.submit((Callable<String>)any);
            result = userDomainDao.findDomainByUser(user);

            userDomainDao.findDomainByUser(user);
            result = domain;

        }};

        String result = userDomainServiceMultiDomainImpl.getDomainForUser(user);
        assertEquals(result, domain);
    }



}

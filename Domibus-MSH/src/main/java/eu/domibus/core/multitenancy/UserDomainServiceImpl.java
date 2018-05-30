package eu.domibus.core.multitenancy;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.user.User;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.core.multitenancy.dao.UserDomainEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class UserDomainServiceImpl implements UserDomainService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDomainServiceImpl.class);

    @Qualifier("taskExecutor")
    @Autowired
    protected SchedulingTaskExecutor schedulingTaskExecutor;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected UserDomainDao userDomainDao;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected UserConverter userConverter;

    /**
     * Get the domain associated to the provided user from the general schema. <br/>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return
     */
    @Override
    public String getDomainForUser(String user) {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOG.debug("Using default domain for user [{}]", user);
            return DomainService.DEFAULT_DOMAIN.getCode();
        }
        LOG.debug("Searching domain for user [{}]", user);
        Future<String> utrFuture = schedulingTaskExecutor.submit(() -> userDomainDao.findDomainByUser(user));
        String domain = null;
        try {
            domain = utrFuture.get(3000L, TimeUnit.SECONDS);
            LOG.debug("Found domain [{}] for user [{}]", domain, user);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AuthenticationServiceException("Could not determine the domain for user [" + user + "]", e);
        }
        return domain;
    }

    /**
     * Get the preferred domain associated to the super user from the general schema. <br/>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return
     */
    @Override
    public  String getPreferredDomainForUser(String user) {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOG.debug("Using default domain for user [{}]", user);
            return DomainService.DEFAULT_DOMAIN.getCode();
        }
        LOG.debug("Searching preferred domain for user [{}]", user);
        Future<String> utrFuture = schedulingTaskExecutor.submit(() -> userDomainDao.findPreferredDomainByUser(user));
        String domain = null;
        try {
            domain = utrFuture.get(3000L, TimeUnit.SECONDS);
            LOG.debug("Found preferred domain [{}] for user [{}]", domain, user);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.debug("Could not determine the preferred domain for user [" + user + "], using default", e);
            domain = DomainService.DEFAULT_DOMAIN.getCode();
        }
        return domain;

    }

    /**
     * Get all super users from the general schema. <br/>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return
     */
    @Override
    public List<User> getSuperUsers() {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            return new ArrayList<>();
        }
        LOG.debug("Searching for super users");
        Future<List<User>> utrFuture = schedulingTaskExecutor.submit(() -> {
            List<eu.domibus.common.model.security.User> userEntities = userDao.listUsers();
            List<eu.domibus.api.user.User> users = userConverter.convert(userEntities);

            // fill in preferred domain
            List<UserDomainEntity> domains = userDomainDao.listPreferredDomains();
            users.forEach(u -> {
                String domainCode = domains.stream().filter(d -> d.getUserName().equals(u.getUserName()))
                        .map(d -> d.getPreferredDomain()).findFirst().orElse(null);
                u.setDomain(domainCode);
            });
            return users;
        });
        List<User> users = null;
        try {
            users = utrFuture.get(3000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainException("Could not find super users", e);
        }
        return users; 
    }
    
    
    @Override
    public void setDomainForUser(String user, String domainCode) {
        LOG.debug("Setting domain [{}] for user [{}]", domainCode, user);
        
        Future utrFuture = schedulingTaskExecutor.submit(() -> {
            userDomainDao.setDomainByUser(user, domainCode);
        });
        try {
            utrFuture.get(3000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainException("Could not set domain", e);
        }
    }
    
    @Override  
    public void setPreferredDomainForUser(String user, String domainCode) {
        LOG.debug("Setting preferred domain [{}] for user [{}]", domainCode, user);
        
        Future utrFuture = schedulingTaskExecutor.submit(() -> {
            userDomainDao.setPreferredDomainByUser(user, domainCode);
        });
        try {
            utrFuture.get(3000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainException("Could not set preferred domain", e);
        }
    }

    @Override
    public List<String> getAllUserNames() {
        LOG.debug("Setting preferred domain [{}] for user [{}]");

        List<String> userNames = null;
        Future<List<String>> utrFuture = schedulingTaskExecutor.submit(() -> {
            return userDomainDao.listAllUserNames();
        });
        try {
            userNames = utrFuture.get(3000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainException("Could not set preferred domain", e);
        }
        return userNames;
    }
}

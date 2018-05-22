package eu.domibus.core.multitenancy;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

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


}

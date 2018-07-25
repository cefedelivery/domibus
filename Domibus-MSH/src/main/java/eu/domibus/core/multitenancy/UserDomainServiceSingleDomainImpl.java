package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel(nperpion)
 * @since 4.0
 */
@Service
public class UserDomainServiceSingleDomainImpl implements UserDomainService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDomainServiceSingleDomainImpl.class);

    /**
     * Get the domain associated to the provided user from the general schema. <br/>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return
     */
    @Override
    public String getDomainForUser(String user) {
        LOG.debug("Using default domain for user [{}]", user);
        return DomainService.DEFAULT_DOMAIN.getCode();
    }

    /**
     * Get the preferred domain associated to the super user from the general schema. <br/>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return
     */
    @Override
    public String getPreferredDomainForUser(String user) {
        return this.getDomainForUser(user);
    }

    /**
     * Get all super users from the general schema. <br/>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return
     */
    @Override
    public List<User> getSuperUsers() {
        return new ArrayList<>();
    }


    @Override
    public void setDomainForUser(String user, String domainCode) {
        return;
    }

    @Override
    public void setPreferredDomainForUser(String user, String domainCode) {
        return;
    }

    @Override
    public void deleteDomainForUser(String user) {
        return;
    }

    /**
     * Retrieves all users from general schema
     */
    @Override
    public List<String> getAllUserNames() {
        return new ArrayList<>();
    }
}

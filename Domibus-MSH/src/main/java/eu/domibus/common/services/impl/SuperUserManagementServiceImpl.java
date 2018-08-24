package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service("superUserManagementService")
public class SuperUserManagementServiceImpl extends UserManagementServiceImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SuperUserManagementServiceImpl.class);

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<eu.domibus.api.user.User> findUsers() {
        List<eu.domibus.api.user.User> allUsers = super.findUsers();
        List<eu.domibus.api.user.User> superUsers = userDomainService.getSuperUsers();
        allUsers.addAll(superUsers);
        return allUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateUsers(List<eu.domibus.api.user.User> users) {
        List<eu.domibus.api.user.User> regularUsers = users.stream()
                .filter(u -> !u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());
        super.updateUsers(regularUsers);

        List<eu.domibus.api.user.User> superUsers = users.stream()
                .filter(u -> u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());

        try {
            domainTaskExecutor.submit(() -> {
                // this block needs to called inside a transaction;
                // for this the whole code inside the block needs to reside into a Spring bean service marked with transaction REQUIRED
                super.updateUsers(superUsers);
                return null;
            });
        } catch (eu.domibus.api.multitenancy.DomainException ex) {
            if (ex.getCause().getCause() instanceof UserManagementException) {
                throw (UserManagementException) ex.getCause().getCause();
            } else {
                throw ex;
            }
        }
    }

}

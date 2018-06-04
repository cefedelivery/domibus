package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
 

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service("superUserManagementService")
public class SuperUserManagementServiceImpl extends UserManagementServiceImpl {

    @Autowired
    protected UserDomainService userDomainService;
    
    @Qualifier("taskExecutor")
    @Autowired
    protected SchedulingTaskExecutor schedulingTaskExecutor;
    
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

        userPersistenceService.updateUsers(regularUsers);

        List<eu.domibus.api.user.User> superUsers = users.stream()
                .filter(u -> u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());

        Future utrFuture = schedulingTaskExecutor.submit(() -> {
            // this block needs to called inside a transaction; 
            // for this the whole code inside the block needs to reside into a Spring bean service marked with transaction REQUIRED
            userPersistenceService.updateUsers(superUsers);
        });

        try {
            utrFuture.get(3000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainException("Could not save super users", e);
        }
    }

}

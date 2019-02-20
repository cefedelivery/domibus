package eu.domibus.security;

import eu.domibus.AbstractIT;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.ConsoleUserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class ConsoleUserSecurityPolicyManagerTestIT extends AbstractIT {

    @Autowired
    ConsoleUserSecurityPolicyManager userSecurityPolicyManager;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected UserDao userDao;

    @Autowired
    private ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private ConsoleUserAlertsServiceImpl userAlertsService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager entityManager;


    private User initTestUser(String userName) {
        UserRole userRole = entityManager.find(UserRole.class, 1);
        if (userRole == null) {
            userRole = new UserRole("ROLE_USER");
            entityManager.persist(userRole);
        }
        User user = new User();
        user.setUserName(userName);
        user.setPassword("Password-0");
        user.addRole(userRole);
        user.setEmail("test@mailinator.com");
        user.setActive(true);
        userDao.create(user);
        return user;
    }

    @Test
    @Transactional
    @Rollback
    public void testPasswordReusePolicy_shouldPass() {
        User user = initTestUser("testUser1");
        userSecurityPolicyManager.changePassword(user, "Password-1");
        userSecurityPolicyManager.changePassword(user, "Password-2");
        userSecurityPolicyManager.changePassword(user, "Password-3");
        userSecurityPolicyManager.changePassword(user, "Password-4");
        userSecurityPolicyManager.changePassword(user, "Password-5");
        userSecurityPolicyManager.changePassword(user, "Password-6");
        userSecurityPolicyManager.changePassword(user, "Password-1");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordReusePolicy_shouldFail() {
        User user = initTestUser("testUser2");
        userSecurityPolicyManager.changePassword(user, "Password-1");
        userSecurityPolicyManager.changePassword(user, "Password-2");
        userSecurityPolicyManager.changePassword(user, "Password-3");
        userSecurityPolicyManager.changePassword(user, "Password-4");
        userSecurityPolicyManager.changePassword(user, "Password-5");
        userSecurityPolicyManager.changePassword(user, "Password-1");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordComplexity_blankPasswordShouldFail() {
        User user = initTestUser("testUser3");
        userSecurityPolicyManager.changePassword(user, "");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordComplexity_shortPasswordShouldFail() {
        User user = initTestUser("testUser4");
        userSecurityPolicyManager.changePassword(user, "Aa-1");
    }
}
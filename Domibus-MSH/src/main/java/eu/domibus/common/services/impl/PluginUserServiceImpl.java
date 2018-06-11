package eu.domibus.common.services.impl;

import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthType;
import eu.domibus.common.services.PluginUserService;
import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.core.security.AuthenticationEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pion
 * @since 4.0
 */
@Service
public class PluginUserServiceImpl implements PluginUserService {

    @Autowired
    @Qualifier("securityAuthenticationDAO")
    private AuthenticationDAO securityAuthenticationDAO;

    @Override
    public List<AuthenticationEntity> findUsers(AuthType authType, AuthRole authRole, String originalUser, String userName, int page, int pageSize) {
        Map<String, Object> filters = createFilterMap(authType, authRole, originalUser, userName);
        return securityAuthenticationDAO.findPaged(page * pageSize, pageSize, "entityId", true, filters);
    }

    @Override
    public long countUsers(AuthType authType, AuthRole authRole, String originalUser, String userName) {
        Map<String, Object> filters = createFilterMap(authType, authRole, originalUser, userName);
        return securityAuthenticationDAO.countEntries(filters);
    }

    @Override
    @Transactional
    public void updateUsers(List<AuthenticationEntity> addedUsers, List<AuthenticationEntity> updatedUsers, List<AuthenticationEntity> removedUsers) {
        addedUsers.forEach(u -> securityAuthenticationDAO.create(u));
        // TODO
    }

    private Map<String, Object> createFilterMap(
            AuthType authType,
            AuthRole authRole,
            String originalUser,
            String userName) {
        HashMap<String, Object> filters = new HashMap<>();
        if (authType != null) {
            filters.put("authType", authType.name());
        }
        if (authRole != null) {
            filters.put("authRoles", authRole.name());
        }
        filters.put("originalUser", originalUser);
        filters.put("username", userName);
        return filters;
    }
}

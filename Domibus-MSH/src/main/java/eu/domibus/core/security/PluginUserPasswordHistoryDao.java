package eu.domibus.core.security;

import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.model.security.User;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface PluginUserPasswordHistoryDao extends UserPasswordHistoryDao<AuthenticationEntity> {
}

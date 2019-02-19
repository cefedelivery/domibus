package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.UserBase;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface UserDaoBase {
    UserBase findByUserName(String userName);

    List<UserBase> findWithPasswordChangedBetween(LocalDate start, LocalDate end, boolean withDefaultPassword);
}

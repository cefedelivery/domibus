package eu.domibus.common.converters;

import eu.domibus.api.user.User;
import eu.domibus.api.user.UserState;
import eu.domibus.common.model.security.UserRole;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Component
public class UserDefaultConverter implements UserConverter {

    @Override
    public User convert(eu.domibus.common.model.security.User userEntity) {
        List<String> authorities = new ArrayList<>();
        Collection<UserRole> roles = userEntity.getRoles();
        for (UserRole role : roles) {
            authorities.add(role.getName());
        }
        return new eu.domibus.api.user.User(
                userEntity.getUserName(),
                userEntity.getEmail(),
                userEntity.getActive(),
                authorities,
                UserState.PERSISTED,
                userEntity.getSuspensionDate(),
                userEntity.isDeleted());
    }

    @Override
    public List<User> convert(List<eu.domibus.common.model.security.User> sourceList) {
        if (sourceList == null) {
            return null;
        }
        List<User> result = new ArrayList<>();
        for (eu.domibus.common.model.security.User sourceObject : sourceList) {
            result.add(convert(sourceObject));
        }
        return result;
    }
}

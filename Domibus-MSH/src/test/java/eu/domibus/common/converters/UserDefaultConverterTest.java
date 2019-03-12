package eu.domibus.common.converters;

import com.google.common.collect.Lists;
import eu.domibus.api.user.UserState;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * @author Sebastian-Ion TINCU
 */
public class UserDefaultConverterTest {

    @Tested
    private UserDefaultConverter userDefaultConverter;

    @Test
    public void setsTheUserNameWhenConvertingUsers() {
        // Given
        User userEntity = new User();
        userEntity.setUserName("userName");

        // When
        eu.domibus.api.user.User converted = userDefaultConverter.convert(userEntity);

        // Then
        Assert.assertEquals("The username should have been correctly set when converting between user types", "userName", converted.getUserName());
    }

    @Test
    public void setsTheEmailWhenConvertingUsers() {
        // Given
        User userEntity = new User();
        userEntity.setEmail("email");

        // When
        eu.domibus.api.user.User converted = userDefaultConverter.convert(userEntity);

        // Then
        Assert.assertEquals("The email should have been correctly set when converting between user types", "email", converted.getEmail());
    }

    @Test
    public void setsTheActiveFlagWhenConvertingUsers() {
        // Given
        User userEntity = new User();
        userEntity.setActive(true);

        // When
        eu.domibus.api.user.User converted = userDefaultConverter.convert(userEntity);

        // Then
        Assert.assertTrue("The active flag should have been correctly set when converting between user types", converted.isActive());
    }

    @Test
    public void setsTheSuspendedFlagToTrueWhenConvertingTheUserHavingSuspensionDate() {
        // Given
        User userEntity = new User();
        userEntity.setSuspensionDate(new Date());

        // When
        eu.domibus.api.user.User converted = userDefaultConverter.convert(userEntity);

        // Then
        Assert.assertTrue("The suspended flag should have been set to true when converting a user having a suspension date", converted.isSuspended());
    }

    @Test
    public void setsTheSuspendedFlagToFalseWhenConvertingTheUserNotHavingSuspensionDate() {
        // Given
        User userEntity = new User();
        userEntity.setSuspensionDate(null);

        // When
        eu.domibus.api.user.User converted = userDefaultConverter.convert(userEntity);

        // Then
        Assert.assertFalse("The suspended flag should have been set to false when converting a user not having a suspension date", converted.isSuspended());
    }

    @Test
    public void setsTheUserStatusToPersistedWhenConvertingUsers() {
        // When
        eu.domibus.api.user.User converted = userDefaultConverter.convert(new User());

        // Then
        Assert.assertEquals("The status should have been correctly set when converting between user types", UserState.PERSISTED.name(), converted.getStatus());
    }

    @Test
    public void setsTheDeletedFlagWhenConvertingUsers() {
        // Given
        User userEntity = new User();
        userEntity.setDeleted(true);

        // When
        eu.domibus.api.user.User converted = userDefaultConverter.convert(userEntity);

        // Then
        Assert.assertTrue("The deleted flag should have been correctly set when converting between user types", converted.isDeleted());
    }

    @Test
    public void populatedAuthoritiesWhenConvertingUsers() {
        // Given
        User userEntity = new User();
        userEntity.setUserName("Neo");
        userEntity.addRole(new UserRole("admin"));
        userEntity.addRole(new UserRole("user"));

        // When
        eu.domibus.api.user.User converted = userDefaultConverter.convert(userEntity);

        // Then
        Assert.assertEquals("The authorities should have been correctly populated when converting between user types", Lists.newArrayList("admin", "user"), converted.getAuthorities());
    }

    @Test
    public void convertsMultipleUsersAtOnce(@Injectable User first, @Injectable User second,
                                            @Injectable eu.domibus.api.user.User firstConverted,
                                            @Injectable eu.domibus.api.user.User secondConverted) {
        // Given
        new Expectations(userDefaultConverter) {{
            userDefaultConverter.convert((User) any); returns(firstConverted, secondConverted);
        }};

        // When
        List<eu.domibus.api.user.User> converted = userDefaultConverter.convert(Lists.newArrayList(first, second));

        // Then
        Assert.assertEquals("Should have converted correctly all users when converting multiple users at once", Lists.newArrayList(firstConverted, secondConverted), converted);
    }


    @Test
    public void returnsNullWhenTryingToConvertMultipleUsersAtOnceAndTheListToConvertIsNull(@Injectable User first, @Injectable User second,
                                            @Injectable eu.domibus.api.user.User firstConverted,
                                            @Injectable eu.domibus.api.user.User secondConverted) {
        // When
        List<eu.domibus.api.user.User> converted = userDefaultConverter.convert((List<User>)null);

        // Then
        Assert.assertNull("Should have returned null when trying to convert multiple users but the list to convert is null", converted);
    }
}
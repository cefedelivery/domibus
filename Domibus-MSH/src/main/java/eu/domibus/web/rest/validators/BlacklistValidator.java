package eu.domibus.web.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * since 4.1
 */
@Component
public class BlacklistValidator implements ConstraintValidator<NotBlacklisted, String> {

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    Character[] blacklist = null;
    @Override
    public void initialize(NotBlacklisted attr) {
        if (blacklist == null) {
            String blacklistValue = domibusPropertyProvider.getProperty("domibus.userInput.blackList");
            if (!Strings.isNullOrEmpty(blacklistValue)) {
                this.blacklist = ArrayUtils.toObject(blacklistValue.toCharArray());
            }
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        try {
            if (ArrayUtils.isEmpty(blacklist)) {
                return true;
            }
            if (Strings.isNullOrEmpty(value)) {
                return true;
            }

            return !Arrays.stream(blacklist).anyMatch(el -> value.contains(el.toString()));

        } catch (Exception e) {
            return false;
        }
    }
}

package eu.domibus.web.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BlacklistValidator implements ConstraintValidator<NotBlacklisted, String> {

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    char[] blacklist = null;

    @Override
    public void initialize(NotBlacklisted attr) {
        if(blacklist == null) {
            String blacklistValue = domibusPropertyProvider.getProperty("domibus.userInput.blackList");
            if (!Strings.isNullOrEmpty(blacklistValue))
                this.blacklist = blacklistValue.toCharArray();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        try {
            if (blacklist == null || blacklist.length == 0) {
                return true;
            }
            if (Strings.isNullOrEmpty(value)) {
                return true;
            }

            // TODO use streams
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                for (int j = 0; j < blacklist.length; j++) {
                    if (c == blacklist[j]) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

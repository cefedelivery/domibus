package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class PasswordPolicyRO implements Serializable {

    private String pattern;
    private String validationMessage;

    public PasswordPolicyRO(String pattern, String validationMessage) {
        this.pattern = pattern;
        this.validationMessage = validationMessage;
    }

    public String getPattern() {
        return pattern;
    }
    public String getValidationMessage() {
        return validationMessage;
    }

}

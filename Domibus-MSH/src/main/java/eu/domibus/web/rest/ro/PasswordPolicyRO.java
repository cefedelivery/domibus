package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.0
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

    public void setPattern(String pattern) { this.pattern = pattern; }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) { this.validationMessage = validationMessage; }

}

package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public class PasswordPolicyRO implements Serializable {

    private String pattern;
    private String validationMessage;
    private boolean checkDefault;

    public PasswordPolicyRO(String pattern, String validationMessage, boolean checkDefault) {
        this.pattern = pattern;
        this.validationMessage = validationMessage;
        this.checkDefault = checkDefault;
    }

    public String getPattern() {
        return pattern;
    }
    public String getValidationMessage() {
        return validationMessage;
    }
    public boolean getCheckDefault() {
        return checkDefault;
    }


//    public void setPattern(String pattern) { this.pattern = pattern; }
//    public void setValidationMessage(String validationMessage) { this.validationMessage = validationMessage; }

}

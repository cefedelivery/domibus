package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class ChangePasswordRO implements Serializable {

    private String currentPassword;
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}

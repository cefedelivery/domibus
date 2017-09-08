package eu.domibus.ext.rest;

import java.io.Serializable;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class ErrorRO implements Serializable {

    protected String message;

    public ErrorRO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

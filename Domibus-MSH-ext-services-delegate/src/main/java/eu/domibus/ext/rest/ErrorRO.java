package eu.domibus.ext.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @JsonIgnore
    public int getContentLength() {
        try {
            return new ObjectMapper().writeValueAsString(this).length();
        } catch (JsonProcessingException e) {
            return -1;
        }
    }
}

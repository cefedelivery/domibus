package eu.domibus.ext.services.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO class for the Messages Monitor Service.
 *
 * It stores information about a delivery attempt for a certain message.
 *
 * @author Federico Martini
 * @since 3.3
 */
public class AttemptDTO implements Serializable {

    /**
     * Id of the message for which the delivery attempt has been performed.
     */
    private String messageId;

    /**
     * Progressive number of the attempt performed
     */
    private int number;

    /**
     * When the attemp has started
     */
    private Date start;

    /**
     * When the attemp has finished
     */
    private Date end;

    /**
     * Gets the progressive number of the attempt.
     *
     * @return
     */
    public int getNumber() {
        return number;
    }

    /**
     * Sets the progressive number of the attempt.
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
    }
}

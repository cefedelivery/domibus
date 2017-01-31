package eu.domibus.ext.services.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO class for the Messages Monitor service
 * It stores information about a delivery attempt.
 *
 * @author Federico Martini
 * @since 3.3
 */
public class AttemptDTO implements Serializable {

    private int number;

    private Date start;

    private Date end;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}

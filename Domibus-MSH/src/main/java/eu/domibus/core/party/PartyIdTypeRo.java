package eu.domibus.core.party;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * DTO class used for party id
 */

public class PartyIdTypeRo {

    protected String name;

    protected String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

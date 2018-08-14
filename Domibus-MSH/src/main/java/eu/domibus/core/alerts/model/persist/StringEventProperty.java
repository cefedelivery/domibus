package eu.domibus.core.alerts.model.persist;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@DiscriminatorValue("STRING")
public class StringEventProperty extends AbstractEventProperty<String> {


    @NotNull
    @Column(name = "STRING_VALUE")
    private String stringValue; //NOSONAR

    @Override
    public String toString() {
        return "EventProperty{" +
                "key='" + getKey() + '\'' +
                ", value='" + stringValue + '\'' +
                '}';
    }


    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String value) {
        this.stringValue = value;
    }

    //using get value to the hibernate property getter causes a polymorphism issue in the criteria api.
    @Override
    public String getValue() {
        return getStringValue();
    }
}

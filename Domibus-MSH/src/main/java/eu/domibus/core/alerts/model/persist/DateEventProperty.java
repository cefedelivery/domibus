package eu.domibus.core.alerts.model.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@DiscriminatorValue("DATE")
public class DateEventProperty extends AbstractEventProperty<Date>{

    @NotNull
    @Column(name = "DATE_VALUE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValue; //NOSONAR

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date value) {
        this.dateValue = value;
    }

    @Override
    public String toString() {
        return "EventProperty{" +
                "key='" + getKey() + '\'' +
                ", date='" + dateValue + '\'' +
                '}';
    }

    @Override
    public Date getValue() {
        return getDateValue();
    }
}

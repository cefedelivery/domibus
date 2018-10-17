package eu.domibus.core.alerts.model.persist;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Table(name = "TB_EVENT_PROPERTY")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity

@NamedQueries({
        @NamedQuery(name = "AbstractEventProperty.findWithTypeAndPropertyValue", query = "SELECT ep.event FROM AbstractEventProperty ep where ep.event.type=:TYPE and ep.key=:PROPERTY and ep.stringValue=:VALUE")
})

public abstract class AbstractEventProperty<T> extends AbstractBaseEntity {

    @NotNull
    @Column(name = "PROPERTY_TYPE")
    protected String key; //NOSONAR

    @NotNull
    @ManyToOne
    @JoinColumn(name="FK_EVENT")
    private Event event; //NOSONAR

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public abstract T getValue();

}

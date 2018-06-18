package eu.domibus.core.alerts.model.persist;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

public class Event extends AbstractBaseEntity {

    private final static Logger LOG = LoggerFactory.getLogger(Event.class);

    @Temporal(TemporalType.TIMESTAMP)
    private Date reportingTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "TB_EVENT_PROPERTY",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "property_id"))
    @MapKey(name = "key")
    private Map<String,EventPropertyValue> properties;

}

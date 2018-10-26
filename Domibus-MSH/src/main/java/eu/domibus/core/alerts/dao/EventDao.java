package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.persist.Event;
import org.springframework.stereotype.Repository;
import javax.persistence.TypedQuery;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class EventDao extends BasicDao<Event> {

    public EventDao() {
        super(Event.class);
    }

    public Event findWithTypeAndPropertyValue(EventType type, String property, String value) {
        TypedQuery<Event> namedQuery = em.createNamedQuery("AbstractEventProperty.findWithTypeAndPropertyValue", Event.class);
        namedQuery.setParameter("TYPE", type);
        namedQuery.setParameter("PROPERTY", property);
        namedQuery.setParameter("VALUE", value);
        return namedQuery.getResultList().stream().findFirst().orElse(null);
    }

}

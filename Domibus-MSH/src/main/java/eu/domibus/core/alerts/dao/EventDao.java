package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.core.alerts.model.persist.Event;
import org.springframework.stereotype.Repository;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class EventDao extends BasicDao<Event> {

    public EventDao() {
        super(Event.class);
    }
}

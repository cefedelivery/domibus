package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.core.alerts.model.persist.Event;
import org.springframework.stereotype.Repository;

@Repository
public class EventDao extends BasicDao<Event> {

    public EventDao() {
        super(Event.class);
    }
}

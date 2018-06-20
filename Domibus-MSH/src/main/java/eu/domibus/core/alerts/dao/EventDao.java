package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.util.AnnotationsUtil;
import eu.domibus.core.alerts.model.persist.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EventDao extends BasicDao<Event> {

    public EventDao() {
        super(Event.class);
    }
}

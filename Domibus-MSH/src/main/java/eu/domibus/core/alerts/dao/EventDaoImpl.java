package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.core.alerts.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDaoImpl<E extends Event> extends BasicDao<E> implements EventDao{

    private final static Logger LOG = LoggerFactory.getLogger(EventDaoImpl.class);

    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public EventDaoImpl(Class<E> typeOfT) {
        super(typeOfT);
    }
}

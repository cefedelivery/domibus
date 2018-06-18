package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.core.alerts.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventDaoImpl<E extends Event> extends BasicDao<E> implements EventDao{


    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public EventDaoImpl(Class<E> typeOfT) {
        super(typeOfT);
    }
}

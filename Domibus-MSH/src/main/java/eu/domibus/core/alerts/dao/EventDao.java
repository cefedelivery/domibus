package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.util.AnnotationsUtil;
import eu.domibus.core.alerts.model.persist.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EventDao extends BasicDao<Event> {


    @Autowired
    private AnnotationsUtil annotationsUtil;
    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public EventDao(Class<Event> typeOfT) {
        super(typeOfT);
    }


}

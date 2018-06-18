package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.util.AnnotationsUtil;
import eu.domibus.core.alerts.model.MessageEvent;
import eu.domibus.core.alerts.model.persist.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public abstract class EventDaoImpl extends BasicDao<Event>{


    @Autowired
    private AnnotationsUtil annotationsUtil;
    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public EventDaoImpl(Class<Event> typeOfT) {
        super(typeOfT);
    }

    public void save(MessageEvent messageEvent){

    }

    private Event transform(MessageEvent messageEvent){
        annotationsUtil.getValue()
        Arrays.stream(messageEvent.getClass().getFields()).map(field -> annotationsUtil.getValue());
    }


}

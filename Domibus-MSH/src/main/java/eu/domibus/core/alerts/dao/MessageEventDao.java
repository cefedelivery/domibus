package eu.domibus.core.alerts.dao;

import eu.domibus.core.alerts.model.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class MessageEventDao extends EventDaoImpl<MessageEvent>{

    private final static Logger LOG = LoggerFactory.getLogger(MessageEventDao.class);

    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public MessageEventDao(Class<MessageEvent> typeOfT) {
        super(typeOfT);
    }

}

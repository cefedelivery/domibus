package eu.domibus.acknowledge.dao;

import eu.domibus.acknowledge.entities.MessageAcknowledge;
import eu.domibus.common.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * Created by migueti on 15/03/2017.
 */
@Repository
@Transactional
public class MessageAcknowledgeHibernateDao extends BasicDao<MessageAcknowledge> implements IMessageAcknowledgeDao {

    public MessageAcknowledgeHibernateDao() {
        super(MessageAcknowledge.class);
    }

    @Override
    public MessageAcknowledge findByCriteria(String messageId, String username, String originalUser) {
        return null;
    }
}

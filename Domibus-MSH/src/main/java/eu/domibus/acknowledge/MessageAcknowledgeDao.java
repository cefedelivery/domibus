package eu.domibus.acknowledge;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.ebms3.common.model.MessageAcknowledgeEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

@Repository
public class MessageAcknowledgeDao extends BasicDao<MessageAcknowledgeEntity> {

    private static final Log LOG = LogFactory.getLog(MessageAcknowledgeDao.class);

    public MessageAcknowledgeDao() {
        super(MessageAcknowledgeEntity.class);
    }

    public MessageAcknowledgeEntity findByCriteria(final String messageId, String username, String originalUser) {

        //TODO implement this
        final TypedQuery<MessageAcknowledgeEntity> query = this.em.createNamedQuery("TODO", MessageAcknowledgeEntity.class);
        query.setParameter("MESSAGE_ID", messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }
}

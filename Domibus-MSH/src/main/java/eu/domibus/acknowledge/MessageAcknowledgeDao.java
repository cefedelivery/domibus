package eu.domibus.acknowledge;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.ebms3.common.model.MessageAcknowledge;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

@Repository
public class MessageAcknowledgeDao extends BasicDao<MessageAcknowledge> {

    private static final Log LOG = LogFactory.getLog(MessageAcknowledgeDao.class);

    public MessageAcknowledgeDao() {
        super(MessageAcknowledge.class);
    }

    public MessageAcknowledge findByCriteria(final String messageId, String username, String originalUser) {

        //TODO implement this
        final TypedQuery<MessageAcknowledge> query = this.em.createNamedQuery("TODO", MessageAcknowledge.class);
        query.setParameter("MESSAGE_ID", messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }
}

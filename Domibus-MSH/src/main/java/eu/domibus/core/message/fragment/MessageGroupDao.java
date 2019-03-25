package eu.domibus.core.message.fragment;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Repository
public class MessageGroupDao extends BasicDao<MessageGroupEntity> {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageGroupDao.class);

    public MessageGroupDao() {
        super(MessageGroupEntity.class);
    }

    public MessageGroupEntity findByGroupId(String groupId) {
        final TypedQuery<MessageGroupEntity> namedQuery = em.createNamedQuery("MessageGroupEntity.findByGroupId", MessageGroupEntity.class);
        namedQuery.setParameter("GROUP_ID", groupId);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException ex) {
            LOG.trace("Could not found MessageGroupEntity for group [{}]", groupId);
            return null;
        }
    }
}

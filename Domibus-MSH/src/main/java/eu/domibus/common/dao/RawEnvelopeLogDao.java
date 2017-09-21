package eu.domibus.common.dao;

import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author idragusa
 * @since 3.2.5
 */
@Repository
public class RawEnvelopeLogDao extends BasicDao<RawEnvelopeLog> {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(RawEnvelopeLogDao.class);

    public RawEnvelopeLogDao() {
        super(RawEnvelopeLog.class);
    }

    //@thom test this class
    public RawEnvelopeDto findRawXmlByMessageId(final String messageId) {
        TypedQuery<RawEnvelopeDto> namedQuery = em.createNamedQuery("find.by.message.id", RawEnvelopeDto.class);
        namedQuery.setParameter("MESSAGE_ID",messageId);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException nr) {
            LOG.warn("The message should have an associate raw xml saved in the database.");
            return null;
        }
    }

    public void deleteRawMessage(final String messageId) {
        TypedQuery<RawEnvelopeLog> namedQuery = em.createNamedQuery("find.entity.by.message.id", RawEnvelopeLog.class);
        namedQuery.setParameter("MESSAGE_ID", messageId);
        final List<RawEnvelopeLog> resultList = namedQuery.getResultList();
        for (RawEnvelopeLog rawEnvelopeLog : resultList) {
            delete(rawEnvelopeLog);
        }
    }


}

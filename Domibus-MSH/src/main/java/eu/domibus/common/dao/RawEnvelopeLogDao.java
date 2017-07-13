package eu.domibus.common.dao;

import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * @author idragusa
 * @since 3.2.5
 */
@Repository
public class RawEnvelopeLogDao extends BasicDao<RawEnvelopeLog> {


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
            return null;
        }
    }

    public void deleteRawMessage(final int id){
        RawEnvelopeLog read = read(id);
        delete(read);
    }


}

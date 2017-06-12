package eu.domibus.common.dao;

import eu.domibus.common.model.logging.RawEnvelopeLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

import static org.springframework.util.StringUtils.hasLength;

/**
 * @author idragusa
 * @since 3.2.5
 */
@Repository
public class RawEnvelopeLogDao extends BasicDao<RawEnvelopeLog> {

    private static final Log LOG = LogFactory.getLog(RawEnvelopeLogDao.class);

    public RawEnvelopeLogDao() {
        super(RawEnvelopeLog.class);
    }

    //@thom test this class
    public String findRawXmlByMessageId(final String messageId){
        TypedQuery<String> namedQuery = em.createNamedQuery("find.by.message.id", String.class);
        namedQuery.setParameter("MESSAGE_ID",messageId);
        return namedQuery.getSingleResult();

    }


}

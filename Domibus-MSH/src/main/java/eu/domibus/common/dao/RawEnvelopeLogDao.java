package eu.domibus.common.dao;

import com.codahale.metrics.Timer;
import eu.domibus.api.metrics.Metrics;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.ebms3.sender.SaveRawPulledMessageInterceptor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author idragusa
 * @since 3.2.5
 */
//@thom test this class
@Repository
public class RawEnvelopeLogDao extends BasicDao<RawEnvelopeLog> {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(RawEnvelopeLogDao.class);

    public RawEnvelopeLogDao() {
        super(RawEnvelopeLog.class);
    }


    public RawEnvelopeDto findRawXmlByMessageId(final String messageId) {
        TypedQuery<RawEnvelopeDto> namedQuery = em.createNamedQuery("RawDto.findByMessageId", RawEnvelopeDto.class);
        namedQuery.setParameter("MESSAGE_ID",messageId);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException nr) {
            LOG.warn("The message should have an associate raw xml saved in the database.");
            return null;
        }
    }

    /**
     * Delete all the raw entries related to a given UserMessage id.
     *
     * @param messageId the id of the message.
     */
    public void deleteUserMessageRawEnvelope(final String messageId) {
        final Timer.Context findMessagePerIdContext = Metrics.METRIC_REGISTRY.timer(name(SaveRawPulledMessageInterceptor.class, "deleteUserMessageRawEnvelope.findByMessageId")).time();
        TypedQuery<RawEnvelopeLog> namedQuery = em.createNamedQuery("Raw.findByMessageId", RawEnvelopeLog.class);
        namedQuery.setParameter("MESSAGE_ID", messageId);
        final List<RawEnvelopeLog> resultList = namedQuery.getResultList();
        findMessagePerIdContext.stop();
        for (RawEnvelopeLog rawEnvelopeLog : resultList) {
            final Timer.Context deleteContext = Metrics.METRIC_REGISTRY.timer(name(SaveRawPulledMessageInterceptor.class, "deleteUserMessageRawEnvelope.delete")).time();
            delete(rawEnvelopeLog);
            deleteContext.stop();
        }
    }


}

package eu.domibus.common.dao;

import eu.domibus.common.MessageStatus;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasLength;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */

@Repository
public class MessagingDao extends BasicDao<Messaging> {

    private static final String FIND_MESSAGING_ON_STATUS_AND_RECEIVER = "select new eu.domibus.ebms3.common.model.MessagePullDto(ul.messageId,ul.received) from UserMessageLog ul where ul.messageId in (SELECT m.userMessage.messageInfo.messageId as id FROM  Messaging m left join m.userMessage.partyInfo.to.partyId as pids where UPPER(pids.value)=UPPER(:PARTY_ID) and m.userMessage.mpc=:MPC) and ul.messageStatus=:MESSAGE_STATUS ORDER BY ul.received";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingDao.class);
    private static final String PARTY_ID = "PARTY_ID";
    private static final String MESSAGE_STATUS = "MESSAGE_STATUS";
    private static final String MPC = "MPC";
    private static final String MESSAGE_ID = "MESSAGE_ID";

    public MessagingDao() {
        super(Messaging.class);
    }

    public UserMessage findUserMessageByMessageId(final String messageId) {

        final TypedQuery<UserMessage> query = this.em.createNamedQuery("Messaging.findUserMessageByMessageId", UserMessage.class);
        query.setParameter(MESSAGE_ID, messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }

    public SignalMessage findSignalMessageByMessageId(final String messageId) {
        final TypedQuery<SignalMessage> query = this.em.createNamedQuery("Messaging.findSignalMessageByMessageId", SignalMessage.class);
        query.setParameter(MESSAGE_ID, messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }

    public Messaging findMessageByMessageId(final String messageId) {
        try {
            final TypedQuery<Messaging> query = em.createNamedQuery("Messaging.findMessageByMessageId", Messaging.class);
            query.setParameter(MESSAGE_ID, messageId);
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Could not find any message for message id[" + messageId + "]");
            return null;
        }
    }

    /**
     * Clears the payloads data for the message with the given messageId.
     *
     * @param messageId the message id.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void clearPayloadData(String messageId) {

        //add messageId to MDC map
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        Query payloadsQuery = em.createNamedQuery("Messaging.findPartInfosForMessage");
        payloadsQuery.setParameter(MESSAGE_ID, messageId);
        List<PartInfo> results = payloadsQuery.getResultList();
        if (results.isEmpty()) {
            return;
        }
        List<PartInfo> databasePayloads = new ArrayList<>();

        for (PartInfo result : results) {
            if (hasLength(result.getFileName())) {
                try {
                    Files.delete(Paths.get(result.getFileName()));
                } catch (IOException e) {
                    LOG.debug("Problem deleting payload data files", e);
                }
            } else {
                databasePayloads.add(result);
            }
        }
        if (!databasePayloads.isEmpty()) {
            final Query emptyQuery = em.createNamedQuery("Messaging.emptyPayloads");
            emptyQuery.setParameter("PARTINFOS", databasePayloads);
            emptyQuery.executeUpdate();
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_DATA_CLEARED, messageId);
    }

    /**
     * Retrieves messages based STATUS and TO fields. The return is ordered by received date.
     * @param partyIdentifier the party to which this message should be delivered.
     * @param messageStatus the status of the message.
     * @param mpc the message partition channel of the message.
     * @return a list of class containing the date and the messageId.
     */
    public List<MessagePullDto> findMessagingOnStatusReceiverAndMpc(final String partyIdentifier, final MessageStatus messageStatus, final String mpc){
        TypedQuery<MessagePullDto> processQuery= em.createQuery(FIND_MESSAGING_ON_STATUS_AND_RECEIVER,MessagePullDto.class);
        processQuery.setParameter(PARTY_ID, partyIdentifier);
        processQuery.setParameter(MESSAGE_STATUS, messageStatus);
        processQuery.setParameter(MPC, mpc);
        return processQuery.getResultList();
    }
}


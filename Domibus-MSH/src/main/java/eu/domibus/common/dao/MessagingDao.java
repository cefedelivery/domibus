package eu.domibus.common.dao;

import eu.domibus.common.MessageStatus;
import eu.domibus.ebms3.common.model.MessagePullDto;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasLength;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */

@Repository
public class MessagingDao extends BasicDao<Messaging> {

    final static String FIND_MESSAGING_ON_STATUS_AND_RECEIVER="select new eu.domibus.ebms3.common.model.MessagePullDto(ul.messageId,ul.received) from UserMessageLog ul where ul.messageId in (SELECT m.userMessage.messageInfo.messageId as id FROM  Messaging m left join m.userMessage.partyInfo.to.partyId as pids where pids.value=:PARTY_ID and m.userMessage.mpc=:MPC) and ul.messageStatus=:MESSAGE_STATUS ORDER BY ul.received";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingDao.class);
    private static final String PARTY_ID = "PARTY_ID";
    private static final String MESSAGE_STATUS = "MESSAGE_STATUS";
    private static final String MPC = "MPC";

    public MessagingDao() {
        super(Messaging.class);
    }

    public UserMessage findUserMessageByMessageId(final String messageId) {

        final TypedQuery<UserMessage> query = this.em.createNamedQuery("Messaging.findUserMessageByMessageId", UserMessage.class);
        query.setParameter("MESSAGE_ID", messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }

    public Messaging findMessageByMessageId(final String messageId) {
        try {
            final TypedQuery<Messaging> query = em.createNamedQuery("Messaging.findMessageByMessageId", Messaging.class);
            query.setParameter("MESSAGE_ID", messageId);
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Could not find any message for message id[" + messageId + "]", nrEx);
            return null;
        }
    }

    /**
     * Clears the payloads data for the message with the given messageId.
     *
     * @param messageId
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void clearPayloadData(String messageId) {
        Query payloadsQuery = em.createNamedQuery("Messaging.findPartInfosForMessage");
        payloadsQuery.setParameter("MESSAGE_ID", messageId);
        List<PartInfo> results = payloadsQuery.getResultList();
        if (results.isEmpty()) {
            return;
        }
        List<PartInfo> databasePayloads = new ArrayList<>();

        for (PartInfo result : results) {
            if (hasLength(result.getFileName())) {
                if (!new File(result.getFileName()).delete()) {
                    LOG.warn("Problem deleting paylod data files");
                    return;
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
     * @param partyId the party to which this message should be delivered.
     * @param messageStatus the status of the message.
     * @param mpc
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


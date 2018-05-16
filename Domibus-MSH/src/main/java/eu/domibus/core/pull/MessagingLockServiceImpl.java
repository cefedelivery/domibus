package eu.domibus.core.pull;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.services.impl.PullServiceImpl;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 3.3.3
 * <p>
 * {@inheritDoc}
 */
@Service
public class MessagingLockServiceImpl implements MessagingLockService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockServiceImpl.class);
    public static final String MPC = "mpc";

    public static final String INITIATOR = "initiator";

    public static final String MESSAGE_TYPE = "messageType";

    public static final String CURRENT_TIME = "current_time";

    @Autowired
    private MessagingLockDao messagingLockDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private PullServiceImpl pullService;



    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("domibusJDBC-XADataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String getPullMessageId(final String initiator, final String mpc) {
        Map<String, Object> params = new HashMap<>();
        params.put(MPC, mpc);
        params.put(INITIATOR, initiator);
        params.put(MESSAGE_TYPE, MessagingLock.PULL);
        params.put(CURRENT_TIME, new Date(System.currentTimeMillis()));
        final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select ID_PK from TB_MESSAGING_LOCK where MESSAGE_STATE = 'READY' and MPC=:mpc and INITIATOR=:initiator AND message_type=:messageType AND (NEXT_ATTEMPT is null  or NEXT_ATTEMPT<:current_time) order by ID_PK", params);
        LOG.debug("Reading messages for initiatior [{}] mpc[{}]", initiator, mpc);
        while (sqlRowSet.next()) {
            final PullMessageId pullMessageId = messagingLockDao.getNextPullMessageToProcess2final(sqlRowSet.getLong(1));
            if (pullMessageId != null) {
                LOG.debug("Message retrieved [{}] \n", pullMessageId);
                final String messageId = pullMessageId.getMessageId();
                switch (pullMessageId.getState()) {
                    case STALED:
                        LOG.trace(pullMessageId.getStaledReason());
                        pullService.messageStaled(messageId);
                        break;
                    case OK:
                        return messageId;
                }
            }
        }
        LOG.debug("Returning null message\n");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addSearchInFormation(final PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog) {
        String partyId = partyIdExtractor.getPartyId();
        final String messageId = messageLog.getMessageId();
        final String mpc = messageLog.getMpc();
        LOG.trace("Saving message lock with id:[{}],partyID:[{}], mpc:[{}]", messageId, partyId, mpc);
        final String pmodeKey; // FIXME: This does not work for signalmessages
        try {
            pmodeKey = this.pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
        } catch (EbMS3Exception e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_001, "Could not get the PMode key for message [" + messageId + "]", e);
        }
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final Date staledDate = pullService.getStaledDate(messageLog, legConfiguration);

        MessagingLock messagingLock = new MessagingLock(
                messageId,
                partyId,
                mpc,
                messageLog.getReceived(),
                staledDate,
                messageLog.getNextAttempt(),
                messageLog.getSendAttempts(),
                messageLog.getSendAttemptsMax());
        messagingLockDao.save(messagingLock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(final String messageId) {
        messagingLockDao.delete(messageId);
    }

}

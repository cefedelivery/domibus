package eu.domibus.core.message.attempt;

import eu.domibus.AbstractIT;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.core.message.acknowledge.MessageAcknowledgementDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
//TODO move it in the core module when Domibus will reference the configuration files via the classpath instead of file disk
public class MessageAttemptDaoTestIT extends AbstractIT {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementDao.class);

    @Autowired
    MessageAttemptDao messageAttemptDao;

    @Before
    public void setup() {
        LOG.debug("Setting up");
    }

    @Test
    @Transactional
    public void testSaveMessageAcknowledge() throws Exception {
        MessageAttemptEntity entity = new MessageAttemptEntity();
        entity.setStartDate(new Timestamp(System.currentTimeMillis()));
        entity.setEndDate(new Timestamp(System.currentTimeMillis()));
        entity.setStatus(MessageAttemptStatus.SUCCESS);
        entity.setMessageId("123");

        messageAttemptDao.create(entity);
        assertTrue(entity.getEntityId() > 0);

        final List<MessageAttemptEntity> entities = messageAttemptDao.findByMessageId("123");
        assertTrue(entities.size() == 1);
        assertEquals(entity, entities.iterator().next());
    }
}


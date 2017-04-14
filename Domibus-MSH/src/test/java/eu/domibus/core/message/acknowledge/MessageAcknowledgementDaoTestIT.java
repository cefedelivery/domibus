package eu.domibus.core.message.acknowledge;

import eu.domibus.AbstractIT;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class MessageAcknowledgementDaoTestIT extends AbstractIT {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementDao.class);

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Autowired
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Before
    public void setup() {
        LOG.debug("Setting up");
    }

    @Test
    @Transactional
    public void testSaveMessageAcknowledge() throws Exception {
        String user = "baciuco";
        String messageId = "123";
        Timestamp acknowledgetTimestamp = new Timestamp(System.currentTimeMillis());
        String from = "C3";
        String to = "C4";
        Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");
        properties.put("prop1", "value1");

        MessageAcknowledgementEntity entity = messageAcknowledgeConverter.create(user, messageId, acknowledgetTimestamp, from, to, properties);
        messageAcknowledgementDao.create(entity);

        final List<MessageAcknowledgementEntity> retrievedEntityList = messageAcknowledgementDao.findByMessageId(messageId);

        assertNotNull(retrievedEntityList);
        assertNotNull(retrievedEntityList.size() == 1);

        final MessageAcknowledgementEntity retrievedEntity = retrievedEntityList.get(0);
        assertEquals(entity.getEntityId(), retrievedEntity.getEntityId());
        assertEquals(entity.getCreateUser(), retrievedEntity.getCreateUser());
        assertEquals(entity.getMessageId(), retrievedEntity.getMessageId());
        assertEquals(entity.getAcknowledgeDate(), retrievedEntity.getAcknowledgeDate());
        assertEquals(entity.getFrom(), retrievedEntity.getFrom());
        assertEquals(entity.getTo(), retrievedEntity.getTo());
        assertEquals(entity.getProperties().iterator().next(), retrievedEntity.getProperties().iterator().next());
    }

}


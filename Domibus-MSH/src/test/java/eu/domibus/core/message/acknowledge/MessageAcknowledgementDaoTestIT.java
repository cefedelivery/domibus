package eu.domibus.core.message.acknowledge;

import eu.domibus.AbstractIT;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.SignalMessageLogBuilder;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
//@ActiveProfiles("h2Debug")
public class MessageAcknowledgementDaoTestIT extends AbstractIT {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementDao.class);

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Autowired
    MessagingDao messagingDao;

    @Autowired
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private SignalMessageDao signalMessageDao;


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

    //@Test
    //@Transactional
    public void testMessaging() throws Exception {
        //TODO: Check why Party From and To are not working
        SignalMessage signalMessage = getSignalMessage();
        signalMessageDao.create(signalMessage);

        Messaging messaging = getMessaging();
        messaging.setSignalMessage(signalMessage);
        messaging.getUserMessage().setMessageInfo(signalMessage.getMessageInfo());
        messagingDao.create(messaging);

        // Builds the signal message log
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setMessageId(messaging.getSignalMessage().getMessageInfo().getMessageId())
                .setMessageStatus(MessageStatus.SEND_IN_PROGRESS)
                .setMshRole(MSHRole.SENDING)
                .setNotificationStatus(NotificationStatus.NOT_REQUIRED);
        // Saves an entry of the signal message log
        signalMessageLogDao.create(smlBuilder.build());

        List<MessageLogInfo> allInfoPaged = signalMessageLogDao.findAllInfoPaged(1, 100, null, false, new HashMap<String, Object>());
        System.out.println("results:" + allInfoPaged);
    }

    protected SignalMessage getSignalMessage() throws Exception {
        String TEST_RESOURCES_DIR = "./src/test/resources";

        File validAS4ResponseFile = new File(TEST_RESOURCES_DIR + "/dataset/as4/validAS4Response.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document responseFileDocument = documentBuilder.parse(validAS4ResponseFile);
        Node messagingNode = responseFileDocument.getElementsByTagName("eb3:Messaging").item(0);
        return JAXBContext.newInstance(Messaging.class).createUnmarshaller().unmarshal(messagingNode, Messaging.class).getValue().getSignalMessage();
    }

    protected Messaging getMessaging() throws Exception {
        String TEST_RESOURCES_DIR = "./src/test/resources";
        File validRequestFile = new File(TEST_RESOURCES_DIR + "/dataset/as4/blue2redGoodMessage.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document responseFileDocument = documentBuilder.parse(validRequestFile);
        final Node messagingNode = responseFileDocument.getElementsByTagName("ns:Messaging").item(0);
        return JAXBContext.newInstance(Messaging.class).createUnmarshaller().unmarshal(messagingNode, Messaging.class).getValue();
    }

}


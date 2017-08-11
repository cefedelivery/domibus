import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class HibernateTool {


    protected EntityManager setUp() throws Exception {
        // A SessionFactory is set up once for an application
        final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("domibusJTA");
        return entityManagerFactory.createEntityManager();
    }

    public HibernateTool() throws Exception {
        final EntityManager entityManager = setUp();
        final Query query = entityManager.createQuery("select new eu.domibus.common.model.logging.MessageLogInfo(log.messageId,log.messageStatus,log.notificationStatus,log.mshRole , log.messageType, log.deleted,log.received,log.sendAttempts, log.sendAttemptsMax,log.nextAttempt,message.collaborationInfo.conversationId, partyFrom.value, partyTo.value, propsFrom.value, propsTo.value, info.refToMessageId) from UserMessageLog log, " +
                "UserMessage message " +
                "left join log.messageInfo info " +
                "left join message.messageProperties.property propsFrom " +
                "left join message.messageProperties.property propsTo " +
                "left join message.partyInfo.from.partyId partyFrom " +
                "left join message.partyInfo.to.partyId partyTo " +
                "where message.messageInfo = info and propsFrom.name = 'originalSender'" +
                "and propsTo.name = 'finalRecipient'");

        //final String query="";
        final List resultList = query.getResultList();
        System.out.println("Result:" + resultList.size());
    }

    public static void main(String[] args) throws Exception {
        new HibernateTool();
    }
}

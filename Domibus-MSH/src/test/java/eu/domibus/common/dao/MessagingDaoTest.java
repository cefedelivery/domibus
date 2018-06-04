package eu.domibus.common.dao;

import eu.domibus.ebms3.common.model.SignalMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.support.DataAccessUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class MessagingDaoTest {

    @Tested
    private MessagingDao messagingDao;

    @Injectable
    private EntityManager entityManager;

    @Mocked
    TypedQuery<SignalMessage> query;

    @Test
    public void testFindSignalMessageByMessageId() {
        // Given
        SignalMessage signalMessage = new SignalMessage();
        String signalMessageId = "test";
        new Expectations() {{
           entityManager.createNamedQuery(anyString, SignalMessage.class);
           result = query;
           DataAccessUtils.singleResult(query.getResultList());
           result = signalMessage;
        }};

        // When
        SignalMessage signalMessageByMessageId = messagingDao.findSignalMessageByMessageId(signalMessageId);

        // Then
        Assert.assertEquals(signalMessage, signalMessageByMessageId);
    }
}

package eu.domibus.core.message.fragment;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;

/**
 * @author Cosmin Baciu
 * @since
 */
public class MessageGroupDefaultServiceTest {

    @Injectable
    protected MessageGroupDao messageGroupDao;

    @Tested
    MessageGroupDefaultService messageGroupDefaultService;


    @Test
    public void setSourceMessageId(@Injectable final MessageGroupEntity messageGroupEntity) {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;

        new Expectations() {{
            messageGroupDao.findByGroupId(groupId);
            result = messageGroupEntity;
        }};

        messageGroupDefaultService.setSourceMessageId(sourceMessageId, groupId);

        new Verifications() {{
            messageGroupEntity.setSourceMessageId(sourceMessageId);
            messageGroupDao.update(messageGroupEntity);
        }};


    }
}
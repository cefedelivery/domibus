package eu.domibus.core.message.fragment;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class MessageGroupDefaultService implements MessageGroupService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageGroupDefaultService.class);

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Override
    public void setSourceMessageId(String sourceMessageId, String groupId) {
        LOG.debug("Updating the SourceMessage id [{}] for group [{}]", sourceMessageId, groupId);
        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        messageGroupEntity.setSourceMessageId(sourceMessageId);
        messageGroupDao.update(messageGroupEntity);
    }
}

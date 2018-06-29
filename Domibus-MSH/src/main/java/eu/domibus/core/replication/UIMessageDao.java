package eu.domibus.core.replication;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.ebms3.common.model.UserMessage;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Component;

import javax.persistence.TypedQuery;

/**
 * @author Catalin Enache
 * @since  4.0
 */
@Component
public class UIMessageDao extends BasicDao<UIMessageEntity> {

    private static final String MESSAGE_ID = "MESSAGE_ID";


    public UIMessageDao() {
        super(UIMessageEntity.class);
    }

    public UIMessageEntity findUIMessageByMessageId(final String messageId) {

        final TypedQuery<UIMessageEntity> query = this.em.createNamedQuery("UIMessageEntity.findUIMessageByMessageId", UIMessageEntity.class);
        query.setParameter(MESSAGE_ID, messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }
}
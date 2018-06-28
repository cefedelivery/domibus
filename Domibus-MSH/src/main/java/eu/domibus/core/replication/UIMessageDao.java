package eu.domibus.core.replication;

import eu.domibus.common.dao.BasicDao;
import org.springframework.stereotype.Component;

@Component
public class UIMessageDao extends BasicDao<UIMessageEntity> {


    public UIMessageDao() {
        super(UIMessageEntity.class);
    }
}
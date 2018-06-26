package eu.domibus.core.alerts.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.core.alerts.model.persist.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class AlertDao extends BasicDao<Alert> {

    private final static Logger LOG = LoggerFactory.getLogger(AlertDao.class);

    public AlertDao() {
        super(Alert.class);
    }

    public List<Alert> findRetryAlerts(){
        final TypedQuery<Alert> namedQuery = em.createNamedQuery("Alert.findRetry", Alert.class);
        return namedQuery.getResultList();
    }

}

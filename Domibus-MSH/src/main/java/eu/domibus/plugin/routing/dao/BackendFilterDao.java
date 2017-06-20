package eu.domibus.plugin.routing.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.plugin.routing.BackendFilterEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
@Transactional
public class BackendFilterDao extends BasicDao<BackendFilterEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFilterDao.class);

    public BackendFilterDao() {
        super(BackendFilterEntity.class);
    }

    public void create(final List<BackendFilterEntity> filters) {
        for (int i = 0; i < filters.size(); i++) {
            final BackendFilterEntity f = filters.get(i);
            f.setIndex(i);
            super.create(f);
        }
    }

    public void update(final List<BackendFilterEntity> filters) {
        for (int i = 0; i < filters.size(); i++) {
            final BackendFilterEntity f = filters.get(i);
            f.setIndex(i);
            super.update(f);
        }
    }

    public List<BackendFilterEntity> findAll() {
        final TypedQuery<BackendFilterEntity> query = em.createNamedQuery("BackendFilter.findEntries", BackendFilterEntity.class);
        try {
            final List result = query.getResultList();
            Collections.sort(result);
            return result;
        } catch (final NoResultException nrEx) {
            LOG.debug("Query BackendFilterEntity.findEntries did not find any result", nrEx);
            return new ArrayList<>();
        }
    }

}

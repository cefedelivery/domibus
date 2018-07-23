package eu.domibus.core.replication;

import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.0
 * {@inheritDoc}
 */
@Repository
public class UIMessageDiffDaoImpl implements UIMessageDiffDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIMessageDiffDaoImpl.class);

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager em;

    public List<UIMessageDiffEntity> findAll() {
        final TypedQuery<UIMessageDiffEntity> query = this.em.createNamedQuery("UIMessageDiffEntity.findDiffMessages", UIMessageDiffEntity.class);
        return query.getResultList();
    }

    public int countAll() {
        final TypedQuery<Long> query = this.em.createNamedQuery("UIMessageDiffEntity.countDiffMessages", Long.class);
        return query.getSingleResult().intValue();
    }

}

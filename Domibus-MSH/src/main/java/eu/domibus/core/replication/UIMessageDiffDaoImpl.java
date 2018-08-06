package eu.domibus.core.replication;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * {@inheritDoc}
 *
 * @author Catalin Enache
 * @since 4.0
 */
@Repository
public class UIMessageDiffDaoImpl implements UIMessageDiffDao {

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager em;

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<UIMessageDiffEntity> findAll() {
        final TypedQuery<UIMessageDiffEntity> query = this.em.createNamedQuery("UIMessageDiffEntity.findDiffMessages", UIMessageDiffEntity.class);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public int countAll() {
        final TypedQuery<Long> query = this.em.createNamedQuery("UIMessageDiffEntity.countDiffMessages", Long.class);
        return query.getSingleResult().intValue();
    }

    /**
     * {@inheritDoc}
     * @param limit
     * @return
     */
    @Override
    public List<UIMessageDiffEntity> findAll(int limit) {
        final TypedQuery<UIMessageDiffEntity> query = this.em.createNamedQuery("UIMessageDiffEntity.findDiffMessages", UIMessageDiffEntity.class);
        if (limit > 0) {
            query.setFirstResult(0);
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

}

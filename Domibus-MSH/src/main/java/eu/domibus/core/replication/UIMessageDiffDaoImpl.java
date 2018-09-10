package eu.domibus.core.replication;

import eu.domibus.api.configuration.DomibusConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

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
    public List<UIMessageDiffEntity> findAllNative() {
        final Query query = this.em.createNamedQuery("UIMessageDiffEntity.findDiffMessages_" +
                domibusConfigurationService.getDataBaseEngine().name().toUpperCase(), UIMessageDiffEntity.class);
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

    @Override
    public int countAllNative() {
        Query query = this.em.createNamedQuery("UIMessageDiffEntity.countDiffMessages_" +
                domibusConfigurationService.getDataBaseEngine().name().toUpperCase());
        Number result = (Number) query.getSingleResult();

        return result.intValue();
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

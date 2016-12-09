package eu.domibus.plugin.webService.dao;

import eu.domibus.plugin.webService.entity.AbstractBaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * A basic DAO implementation providing the standard CRUD operations,
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

public abstract class BasicDao<T extends AbstractBaseEntity> {
    protected static final Logger LOG = LoggerFactory.getLogger(BasicDao.class);
    private final Class<T> typeOfT;

    @PersistenceContext
    protected EntityManager em;

    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public BasicDao(final Class<T> typeOfT) {
        this.typeOfT = typeOfT;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void create(final T entity) {
        this.em.persist(entity);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final T entity) {
        this.em.remove(this.em.merge(entity));
    }

    public T read(final int id) {
        return this.em.find(this.typeOfT, id);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateAll(final Collection<T> update) {
        for (final T t : update) {
            this.update(t);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void update(final T entity) {
        this.em.merge(entity);
    }
}

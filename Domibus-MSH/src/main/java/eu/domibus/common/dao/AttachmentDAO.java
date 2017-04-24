package eu.domibus.common.dao;

import eu.domibus.ebms3.common.model.PartInfo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * DAO to access binary payload data of a {@link PartInfo} object
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Repository
public class AttachmentDAO {

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager em;

    public byte[] loadBinaryData(final int entityId) {
        final Query q = this.em.createNamedQuery("PartInfo.loadBinaryData");
        q.setParameter("ENTITY_ID", entityId);
        return (byte[]) q.getSingleResult();
    }
}

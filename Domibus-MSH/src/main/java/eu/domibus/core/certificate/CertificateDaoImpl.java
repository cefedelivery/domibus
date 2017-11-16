package eu.domibus.core.certificate;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.certificate.Certificate;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class CertificateDaoImpl extends BasicDao<Certificate> implements CertificateDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateDaoImpl.class);

    public CertificateDaoImpl() {
        super(Certificate.class);
    }

    @Override
    public void saveOrUpdate(final Certificate certificate) {
        Optional<Certificate> byAlias = getByAlias(certificate.getAlias());
        if (byAlias.isPresent()) {
            certificate.setEntityId(byAlias.get().getEntityId());
            em.merge(certificate);
            return;
        }
        em.persist(certificate);
    }

    @Override
    public List<Certificate> getCloseToRevocation(final Date startDate, final Date endDate) {
        Date currentDate = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        TypedQuery<Certificate> namedQuery = em.createNamedQuery("Certificate.findCloseToRevocation", Certificate.class);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching for certificate close to revocation:");
            LOG.debug(" revocation date between [{}] and [{}] and not yet notified for current date [{}]", startDate, endDate, currentDate);
        }
        namedQuery.setParameter("START_DATE", startDate);
        namedQuery.setParameter("END_DATE", endDate);
        namedQuery.setParameter("CURRENT_DATE", currentDate);
        return namedQuery.getResultList();
    }

    @Override
    public void notifyRevocation(final Certificate certificate) {
        Date currentDate = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        certificate.setLastNotification(currentDate);
        em.merge(certificate);
    }

    protected Optional<Certificate> getByAlias(final String alias) {
        TypedQuery<Certificate> namedQuery = em.createNamedQuery("Certificate.findByAlias", Certificate.class);
        namedQuery.setParameter("ALIAS", alias);
        try {
            return Optional.of(namedQuery.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }
}

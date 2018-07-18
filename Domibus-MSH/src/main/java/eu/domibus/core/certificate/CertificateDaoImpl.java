package eu.domibus.core.certificate;

import eu.domibus.api.util.DateUtil;
import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.certificate.Certificate;
import eu.domibus.common.model.certificate.CertificateStatus;
import eu.domibus.common.model.certificate.CertificateType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class CertificateDaoImpl extends BasicDao<Certificate> implements CertificateDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateDaoImpl.class);

    @Autowired
    private DateUtil dateUtil;

    public CertificateDaoImpl() {
        super(Certificate.class);
    }

    @Override
    public void saveOrUpdate(final Certificate certificate) {
        Certificate byAliasAndType = getByAliasAndType(certificate.getAlias(), certificate.getCertificateType());
        if (byAliasAndType != null) {
            LOG.debug("Updating certificate [{}]", certificate);
            certificate.setEntityId(byAliasAndType.getEntityId());
            certificate.setLastNotification(byAliasAndType.getLastNotification());
            em.merge(certificate);
            return;
        }
        LOG.debug("Saving certificate [{}]", certificate);
        em.persist(certificate);
    }

    @Override
    public List<Certificate> findImminentExpirationToNotifyAsAlert(final Date nextNotification, final Date offset) {
        TypedQuery<Certificate> namedQuery = em.createNamedQuery("Certificate.findImminentExpirationToNotifyCertificate", Certificate.class);
        namedQuery.setParameter("NEXT_NOTIFICATION", nextNotification);
        namedQuery.setParameter("OFFSET_DATE", offset);
        return namedQuery.getResultList();
    }

    @Override
    public List<Certificate> findExpiredToNotifyAsAlert(final Date nextNotification, final Date endNotification) {
        TypedQuery<Certificate> namedQuery = em.createNamedQuery("Certificate.findExpiredToNotifyCertificate", Certificate.class);
        namedQuery.setParameter("NEXT_NOTIFICATION", nextNotification);
        namedQuery.setParameter("END_NOTIFICATION", endNotification);
        return namedQuery.getResultList();
    }

    @Override
    public List<Certificate> getUnNotifiedSoonRevoked() {
        return findByStatusForCurrentDate(CertificateStatus.SOON_REVOKED);
    }

    @Override
    public List<Certificate> getUnNotifiedRevoked() {
        return findByStatusForCurrentDate(CertificateStatus.REVOKED);
    }

    protected List<Certificate> findByStatusForCurrentDate(final CertificateStatus certificateStatus) {
        Date currentDate = dateUtil.getStartOfDay();
        TypedQuery<Certificate> namedQuery = em.createNamedQuery("Certificate.findByStatusAndNotificationDate", Certificate.class);
        LOG.debug("Searching certificate with status [{}] for current date [{}]", certificateStatus, currentDate);
        namedQuery.setParameter("CERTIFICATE_STATUS", certificateStatus);
        namedQuery.setParameter("CURRENT_DATE", currentDate);
        return namedQuery.getResultList();
    }

    @Override
    public void updateRevocation(final Certificate certificate) {
        Date currentDate = dateUtil.getStartOfDay();
        certificate.setLastNotification(currentDate);
        em.merge(certificate);
    }

    protected Certificate getByAliasAndType(final String alias, final CertificateType certificateType) {
        TypedQuery<Certificate> namedQuery = em.createNamedQuery("Certificate.findByAliasAndType", Certificate.class);
        namedQuery.setParameter("ALIAS", alias);
        namedQuery.setParameter("CERTIFICATE_TYPE", certificateType);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }


}

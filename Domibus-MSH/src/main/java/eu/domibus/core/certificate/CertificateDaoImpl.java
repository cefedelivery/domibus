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
        if (byAliasAndType!=null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Updating certificate [{}]", certificate);
            }
            certificate.setEntityId(byAliasAndType.getEntityId());
            em.merge(certificate);
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving certificate [{}]", certificate);
        }
        em.persist(certificate);
    }


    @Override
    public List<Certificate> getUnNotifiedSoonRevoked() {
        return findOnStatusAndNotificationDate(CertificateStatus.SOON_REVOKED);
    }

    @Override
    public List<Certificate> getUnNotifiedRevoked() {
        return findOnStatusAndNotificationDate(CertificateStatus.REVOKED);
    }

    protected List<Certificate> findOnStatusAndNotificationDate(final CertificateStatus certificateStatus) {
        Date currentDate = dateUtil.getStartOfDay();
        TypedQuery<Certificate> namedQuery = em.createNamedQuery("Certificate.findOnStatusAndNotificationDate", Certificate.class);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching certificate with status [{}] for current date [{}]", certificateStatus, currentDate);
        }
        namedQuery.setParameter("CERTIFICATE_STATUS", certificateStatus);
        namedQuery.setParameter("CURRENT_DATE", currentDate);
        return namedQuery.getResultList();
    }

    @Override
    public void notifyRevocation(final Certificate certificate) {
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

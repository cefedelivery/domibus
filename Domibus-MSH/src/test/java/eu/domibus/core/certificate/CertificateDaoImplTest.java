package eu.domibus.core.certificate;

import eu.domibus.audit.InMemoryDataBaseConfig;
import eu.domibus.audit.OracleDataBaseConfig;
import eu.domibus.common.model.certificate.Certificate;
import eu.domibus.common.model.certificate.CertificateStatus;
import eu.domibus.common.model.certificate.CertificateType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class,
        OracleDataBaseConfig.class, CertificateDaoImplTest.CertificateDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class CertificateDaoImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateDaoImplTest.class);

    //needed because CertificateDaoImpl implements an interface, so spring tries to convert it to interface based
    //proxy. But one of the method tested is not declared in the interface.
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Configuration
    static public class CertificateDaoConfig {

        @Bean
        public CertificateDaoImpl certificateDao() {
            return new CertificateDaoImpl();
        }
    }

    @PersistenceContext
    private javax.persistence.EntityManager em;

    @Autowired
    private CertificateDaoImpl certificateDao;

    @Test
    @Transactional
    public void saveOrUpdate() throws Exception {

        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + 10000);

        Date notBeforeChanged = new Date(notAfter.getTime() + 20000);
        Date notAfterChanged = new Date(notBeforeChanged.getTime() + 100000);

        String firstCertificateName = "firstCertificateName";
        String secondCertificateName = "secondCertificateName";

        Certificate firstCertificate = new Certificate();
        firstCertificate.setAlias(firstCertificateName);
        firstCertificate.setNotBefore(notBefore);
        firstCertificate.setNotAfter(notAfter);
        firstCertificate.setCertificateType(CertificateType.PUBLIC);
        firstCertificate.setCertificateStatus(CertificateStatus.OK);

        certificateDao.saveOrUpdate(firstCertificate);

        Certificate secondCertificate = new Certificate();
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(notBefore);
        secondCertificate.setNotAfter(notAfter);
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);

        em.persist(secondCertificate);

        secondCertificate = new Certificate();
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(notBeforeChanged);
        secondCertificate.setNotAfter(notAfterChanged);
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);

        certificateDao.saveOrUpdate(secondCertificate);

        Certificate certificate = certificateDao.getByAliasAndType(firstCertificateName, CertificateType.PUBLIC).get();
        assertEquals(notBefore, certificate.getNotBefore());
        assertEquals(notAfter, certificate.getNotAfter());

        certificate = certificateDao.getByAliasAndType(secondCertificateName, CertificateType.PUBLIC).get();
        assertEquals(notBeforeChanged, certificate.getNotBefore());
        assertEquals(notAfterChanged, certificate.getNotAfter());

    }

    @Test
    @Transactional
    public void findByAlias() throws Exception {

        Certificate firstCertificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        firstCertificate.setAlias(firstCertificateName);
        firstCertificate.setNotBefore(new Date());
        firstCertificate.setNotAfter(new Date());
        firstCertificate.setCertificateType(CertificateType.PUBLIC);
        firstCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);

        Certificate secondCertificate = new Certificate();
        String secondCertificateName = "secondCertificateName";
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(new Date());
        secondCertificate.setNotAfter(new Date());
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);

        em.persist(firstCertificate);
        em.persist(secondCertificate);

        assertEquals(firstCertificate, certificateDao.getByAliasAndType(firstCertificateName, CertificateType.PUBLIC).get());
        assertEquals(secondCertificate, certificateDao.getByAliasAndType(secondCertificateName, CertificateType.PUBLIC).get());
        assertFalse(certificateDao.getByAliasAndType("wrongAlias", CertificateType.PUBLIC).isPresent());

    }

    /**
     * In this scenario, notAfter is outside of the start/end range so
     * the method should not return the certificate.
     */
    @Test
    @Transactional
    public void getUnNotifiedSoonRevoked() {
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(new Date());
        em.persist(certificate);
        List<Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedSoonRevoked();
        assertEquals(1, unNotifiedRevoked.size());

        certificate.setLastNotification(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        em.persist(certificate);
        unNotifiedRevoked = certificateDao.getUnNotifiedSoonRevoked();
        assertEquals(0, unNotifiedRevoked.size());
    }

    /**
     * In this scenario, notAfter is between start / end date and notification date is null, so
     * the method should return the certificate.
     */
    @Test
    @Transactional
    public void getUnNotifiedRevoked() {

        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(new Date());
        em.persist(certificate);
        List<Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedRevoked();
        assertEquals(1, unNotifiedRevoked.size());

        certificate.setLastNotification(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        em.persist(certificate);
        unNotifiedRevoked = certificateDao.getUnNotifiedRevoked();
        assertEquals(0, unNotifiedRevoked.size());
    }

}
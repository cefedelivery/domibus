package eu.domibus.core.certificate;

import eu.domibus.audit.InMemoryDataBaseConfig;
import eu.domibus.audit.OracleDataBaseConfig;
import eu.domibus.common.model.certificate.Certificate;
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

        certificateDao.saveOrUpdate(firstCertificate);

        Certificate secondCertificate = new Certificate();
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(notBefore);
        secondCertificate.setNotAfter(notAfter);

        em.persist(secondCertificate);

        secondCertificate = new Certificate();
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(notBeforeChanged);
        secondCertificate.setNotAfter(notAfterChanged);

        certificateDao.saveOrUpdate(secondCertificate);

        Certificate certificate = certificateDao.getByAlias(firstCertificateName).get();
        assertEquals(notBefore, certificate.getNotBefore());
        assertEquals(notAfter, certificate.getNotAfter());

        certificate = certificateDao.getByAlias(secondCertificateName).get();
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

        Certificate secondCertificate = new Certificate();
        String secondCertificateName = "secondCertificateName";
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(new Date());
        secondCertificate.setNotAfter(new Date());

        em.persist(firstCertificate);
        em.persist(secondCertificate);

        assertEquals(firstCertificate, certificateDao.getByAlias(firstCertificateName).get());
        assertEquals(secondCertificate, certificateDao.getByAlias(secondCertificateName).get());
        assertFalse(certificateDao.getByAlias("wrongAlias").isPresent());

    }

    /**
     * In this scenario, notAfter is outside of the start/end range so
     * the method should not return the certificate.
     */
    @Test
    @Transactional
    public void getCloseToRevocationWithNothingToLog() {
        LocalDate now = LocalDate.now();
        LocalDate notAfter = now.plusDays(5);
        LocalDate startDate = now.plusDays(6);
        LocalDate endDate = now.plusDays(10);

        Certificate firstCertificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        firstCertificate.setAlias(firstCertificateName);
        firstCertificate.setNotBefore(new Date());
        firstCertificate.setNotAfter(Date.from(notAfter.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        em.persist(firstCertificate);

        List<Certificate> closeToRevocation = certificateDao.getCloseToRevocation(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        assertEquals(0, closeToRevocation.size());
    }

    /**
     * In this scenario, notAfter is between start / end date and notification date is null, so
     * the method should return the certificate.
     */
    @Test
    @Transactional
    public void getCloseToRevocationWithOneCertificateToLog() {
        LocalDate now = LocalDate.now();
        LocalDate notAfter = now.plusDays(5);
        LocalDate startDate = now.plusDays(4);
        LocalDate endDate = now.plusDays(10);

        Certificate firstCertificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        firstCertificate.setAlias(firstCertificateName);
        firstCertificate.setNotBefore(new Date());
        firstCertificate.setNotAfter(Date.from(notAfter.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        em.persist(firstCertificate);
        LOG.info("Persisted certificate [{}]", firstCertificate);

        List<Certificate> closeToRevocation = certificateDao.getCloseToRevocation(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        assertEquals(1, closeToRevocation.size());
        closeToRevocation.forEach(certificate -> certificateDao.notifyRevocation(certificate));
        closeToRevocation = certificateDao.getCloseToRevocation(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        assertEquals(0, closeToRevocation.size());
    }

    /**
     * In this scenario, notAfter is between start / end date and notification date is < then current date, so
     * the method should return the certificate.
     */
    @Test
    @Transactional
    public void getCloseToRevocationWithOneCertificateToLogDueToNotificationDate() {
        LocalDate now = LocalDate.now();
        LocalDate notAfter = now.plusDays(5);
        LocalDate startDate = now.plusDays(4);
        LocalDate endDate = now.plusDays(10);

        Certificate firstCertificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        firstCertificate.setAlias(firstCertificateName);
        firstCertificate.setNotBefore(new Date());
        firstCertificate.setNotAfter(Date.from(notAfter.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstCertificate.setLastNotification(Date.from(now.minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        em.persist(firstCertificate);
        LOG.info("Persisted certificate [{}]", firstCertificate);

        List<Certificate> closeToRevocation = certificateDao.getCloseToRevocation(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        assertEquals(1, closeToRevocation.size());
        closeToRevocation.forEach(certificate -> certificateDao.notifyRevocation(certificate));
    }

    /**
     * In this scenario, notAfter is between start / end date and notification date == current date, so
     * the method should not return the certificate.
     */
    @Test
    @Transactional
    public void getCloseToRevocationWithNoCertificateToLogDueToNotificationDate() {
        LocalDate now = LocalDate.now();
        LocalDate notAfter = now.plusDays(5);
        LocalDate startDate = now.plusDays(4);
        LocalDate endDate = now.plusDays(10);

        Certificate firstCertificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        firstCertificate.setAlias(firstCertificateName);
        firstCertificate.setNotBefore(new Date());
        firstCertificate.setNotAfter(Date.from(notAfter.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstCertificate.setLastNotification(Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        em.persist(firstCertificate);
        LOG.info("Persisted certificate [{}]", firstCertificate);

        List<Certificate> closeToRevocation = certificateDao.getCloseToRevocation(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        assertEquals(0, closeToRevocation.size());
    }

}
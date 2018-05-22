package eu.domibus.core.certificate;

import eu.domibus.InMemoryDataBaseConfig;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.model.certificate.Certificate;
import eu.domibus.common.model.certificate.CertificateStatus;
import eu.domibus.common.model.certificate.CertificateType;
import eu.domibus.util.DateUtilImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class,
         CertificateDaoImplTest.CertificateDaoConfig.class})
public class CertificateDaoImplTest {


    //needed because CertificateDaoImpl implements an interface, so spring tries to convert it to interface based
    //proxy. But one of the method tested is not declared in the interface.
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Configuration
    static public class CertificateDaoConfig {

        @Bean
        public CertificateDaoImpl certificateDao() {
            return new CertificateDaoImpl();
        }

        @Bean
        public DateUtil dateUtil(){
            return new DateUtilImpl();
        }
    }

    @PersistenceContext
    private javax.persistence.EntityManager em;

    @Autowired
    private CertificateDaoImpl certificateDao;

    @Autowired
    private DateUtil dateUtil;

    @Test(expected = javax.validation.ConstraintViolationException.class)
    @Transactional
    public void saveWithNullDates(){
        Certificate firstCertificate = new Certificate();
        firstCertificate.setAlias("whatEver");
        firstCertificate.setNotBefore(null);
        firstCertificate.setNotAfter(null);
        firstCertificate.setCertificateType(CertificateType.PUBLIC);
        firstCertificate.setCertificateStatus(CertificateStatus.OK);
        certificateDao.saveOrUpdate(firstCertificate);
        em.flush();
    }

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
        secondCertificate.setLastNotification(notBefore);

        em.persist(secondCertificate);

        secondCertificate = new Certificate();
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(notBeforeChanged);
        secondCertificate.setNotAfter(notAfterChanged);
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);

        certificateDao.saveOrUpdate(secondCertificate);

        Certificate certificate = certificateDao.getByAliasAndType(firstCertificateName, CertificateType.PUBLIC);
        assertEquals(notBefore, certificate.getNotBefore());
        assertEquals(notAfter, certificate.getNotAfter());

        certificate = certificateDao.getByAliasAndType(secondCertificateName, CertificateType.PUBLIC);
        assertEquals(notBeforeChanged, certificate.getNotBefore());
        assertEquals(notAfterChanged, certificate.getNotAfter());
        assertEquals(notBefore,certificate.getLastNotification());

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

        assertEquals(firstCertificate, certificateDao.getByAliasAndType(firstCertificateName, CertificateType.PUBLIC));
        assertEquals(secondCertificate, certificateDao.getByAliasAndType(secondCertificateName, CertificateType.PUBLIC));
        assertNull(certificateDao.getByAliasAndType("wrongAlias", CertificateType.PUBLIC));

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

        certificate.setLastNotification(dateUtil.getStartOfDay());
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

        certificate.setLastNotification(dateUtil.getStartOfDay());
        em.persist(certificate);
        unNotifiedRevoked = certificateDao.getUnNotifiedRevoked();
        assertEquals(0, unNotifiedRevoked.size());
    }

}
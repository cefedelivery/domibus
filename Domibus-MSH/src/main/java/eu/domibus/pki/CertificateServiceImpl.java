package eu.domibus.pki;

import com.google.common.collect.Lists;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.model.certificate.CertificateStatus;
import eu.domibus.common.model.certificate.CertificateType;
import eu.domibus.core.alerts.model.service.ExpiredCertificateConfiguration;
import eu.domibus.core.alerts.model.service.ImminentExpirationCertificateConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import eu.domibus.core.certificate.CertificateDao;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.*;

import static eu.domibus.logging.DomibusMessageCode.SEC_CERTIFICATE_REVOKED;
import static eu.domibus.logging.DomibusMessageCode.SEC_CERTIFICATE_SOON_REVOKED;

/**
 * @Author Cosmin Baciu
 * @Since 3.2
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateServiceImpl.class);

    public static final String REVOCATION_TRIGGER_OFFSET_PROPERTY = "domibus.certificate.revocation.offset";

    public static final String REVOCATION_TRIGGER_OFFSET_DEFAULT_VALUE = "15";

    @Autowired
    CRLService crlService;

    @Autowired
    MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    DomainContextProvider domainProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private CertificateDao certificateDao;

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private PModeProvider pModeProvider;

    @Override
    @Transactional(noRollbackFor = DomibusCertificateException.class)
    public boolean isCertificateChainValid(KeyStore trustStore, String alias) throws DomibusCertificateException {
        X509Certificate[] certificateChain = null;
        try {
            certificateChain = getCertificateChain(trustStore, alias);
        } catch (KeyStoreException e) {
            throw new DomibusCertificateException("Error getting the certificate chain from the truststore for [" + alias + "]", e);
        }
        if (certificateChain == null || certificateChain.length == 0 || certificateChain[0] == null) {
            throw new DomibusCertificateException("Could not find alias in the truststore [" + alias + "]");
        }

        for (X509Certificate certificate : certificateChain) {
            boolean certificateValid = isCertificateValid(certificate);
            if (!certificateValid) {
                return false;
            }
        }

        return true;
    }

    protected X509Certificate[] getCertificateChain(KeyStore trustStore, String alias) throws KeyStoreException {
        //TODO get the certificate chain manually based on the issued by info from the original certificate
        final Certificate[] certificateChain = trustStore.getCertificateChain(alias);
        if (certificateChain == null) {
            X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
            return new X509Certificate[]{certificate};
        }
        return Arrays.copyOf(certificateChain, certificateChain.length, X509Certificate[].class);

    }

    @Override
    public boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException {
        boolean isValid = checkValidity(cert);
        if (!isValid) {
            LOG.warn("Certificate is not valid: " + cert);
            return false;
        }
        try {
            return !crlService.isCertificateRevoked(cert);
        } catch (Exception e) {
            throw new DomibusCertificateException(e);
        }
    }

    protected boolean checkValidity(X509Certificate cert) {
        boolean result = false;
        try {
            cert.checkValidity();
            result = true;
        } catch (Exception e) {
            LOG.warn("Certificate is not valid " + cert, e);
        }

        return result;
    }

    @Override
    public String extractCommonName(final X509Certificate certificate) throws InvalidNameException {

        final String dn = certificate.getSubjectDN().getName();
        LOG.debug("DN is: " + dn);
        final LdapName ln = new LdapName(dn);
        for (final Rdn rdn : ln.getRdns()) {
            if (StringUtils.equalsIgnoreCase(rdn.getType(), "CN")) {
                LOG.debug("CN is: " + rdn.getValue());
                return rdn.getValue().toString();
            }
        }
        throw new IllegalArgumentException("The certificate does not contain a common name (CN): " + certificate.getSubjectDN().getName());
    }

    /**
     * Load certificate with alias from JKS file and return as {@code X509Certificate}.
     *
     * @param filePath
     * @param alias
     * @param password
     * @return
     */
    @Override
    public X509Certificate loadCertificateFromJKSFile(String filePath, String alias, String password) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, password.toCharArray());

            Certificate cert = keyStore.getCertificate(alias);

            return (X509Certificate) cert;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            LOG.error("Could not load certificate from file " + filePath + ", alias " + alias + "pass " + password);
            throw new DomibusCertificateException("Could not load certificate from file " + filePath + ", alias " + alias, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TrustStoreEntry> getTrustStoreEntries(final KeyStore trustStore) {
        try {
            List<TrustStoreEntry> trustStoreEntries = new ArrayList<>();
            final Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                final X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                TrustStoreEntry trustStoreEntry = createTrustStoreEntry(alias, certificate);
                trustStoreEntries.add(trustStoreEntry);
            }
            return trustStoreEntries;
        } catch (KeyStoreException e) {
            LOG.warn(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCertificateAndLogRevocation(final Domain currentDomain) {
        final KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(currentDomain);
        final KeyStore keyStore = multiDomainCertificateProvider.getKeyStore(currentDomain);
        saveCertificateData(trustStore, keyStore);
        logCertificateRevocationWarning();
    }

    @Override
    public void validateLoadOperation(ByteArrayInputStream newTrustStoreBytes, String password) {
        try {
            KeyStore tempTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            tempTrustStore.load(newTrustStoreBytes, password.toCharArray());
            newTrustStoreBytes.reset();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new DomibusCertificateException("Could not load key store", e);
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendCertificateAlerts() {
        sendCertificateImminentExpirationAlerts();
        sendCertificateExpiredAlerts();
    }

    private void sendCertificateImminentExpirationAlerts() {
        final ImminentExpirationCertificateConfiguration imminentExpirationCertificateConfiguration = multiDomainAlertConfigurationService.getImminentExpirationCertificateConfiguration();
        final Boolean activeModule = imminentExpirationCertificateConfiguration.getImminentExpirationActive();
        LOG.debug("Certificate Imminent expiration alert module activated:[{}]", activeModule);
        if (!activeModule) {
            return;
        }
        String partyName = null;
        if (pModeProvider.isConfigurationLoaded()) {
            partyName = pModeProvider.getGatewayParty().getName();
        }
        final String accessPoint = partyName;
        final Integer imminentExpirationDelay = imminentExpirationCertificateConfiguration.getImminentExpirationDelay();
        final Date offset = LocalDateTime.now().plusDays(imminentExpirationDelay).toDate();

        final Integer imminentExpirationFrequency = imminentExpirationCertificateConfiguration.getImminentExpirationFrequency();
        final Date notificationDate = LocalDateTime.now().minusDays(imminentExpirationFrequency).toDate();

        LOG.debug("Searching for certificate about to expire with notification date smaller then:[{}] and end date < then current date + offset[{}]->[{}]", notificationDate, imminentExpirationDelay, offset);
        certificateDao.findImminentExpirationToNotify(notificationDate, offset).forEach(certificate -> {
            certificate.setAlertNotificationDate(LocalDateTime.now().withTime(0, 0, 0, 0).toDate());
            certificateDao.saveOrUpdate(certificate);
            final String alias = certificate.getAlias();
            final String accesPointOrAlias = accessPoint == null ? alias : accessPoint;
            eventService.enqueueImminentCertificateExpirationEvent(accesPointOrAlias, alias, new Date());
        });
    }

    private void sendCertificateExpiredAlerts() {
        final ExpiredCertificateConfiguration expiredCertificateConfiguration = multiDomainAlertConfigurationService.getExpiredCertificateConfiguration();
        final boolean activeModule = expiredCertificateConfiguration.isActive();
        LOG.debug("Certificate expired alert module activated:[{}]", activeModule);
        if (!activeModule) {
            return;
        }
        String partyName = null;
        if (pModeProvider.isConfigurationLoaded()) {
            partyName = pModeProvider.getGatewayParty().getName();
        }
        final String accessPoint = partyName;
        final Integer revokedDuration = expiredCertificateConfiguration.getExpiredDuration();
        final Integer revokedFrequency = expiredCertificateConfiguration.getExpiredFrequency();

        Date endNotification = LocalDateTime.now().minusDays(revokedDuration).toDate();
        Date notificationDate = LocalDateTime.now().minusDays(revokedFrequency).toDate();

        certificateDao.findExpiredToNotify(endNotification, notificationDate).forEach(certificate -> {
            certificate.setAlertNotificationDate(LocalDateTime.now().withTime(0, 0, 0, 0).toDate());
            certificateDao.saveOrUpdate(certificate);
            final String alias = certificate.getAlias();
            final String accesPointOrAlias = accessPoint == null ? alias : accessPoint;
            eventService.enqueueCertificateExpiredEvent(accesPointOrAlias, alias, new Date());
        });
    }


    /**
     * Create or update certificate in the db.
     */
    protected void saveCertificateData(KeyStore trustStore, KeyStore keyStore) {
        List<eu.domibus.common.model.certificate.Certificate> certificates = groupAllKeystoreCertificates(trustStore, keyStore);
        for (eu.domibus.common.model.certificate.Certificate certificate : certificates) {
            certificateDao.saveOrUpdate(certificate);
        }
    }

    /**
     * Load expired and soon expired certificates, add a warning of error log, and save a flag saying the system already
     * notified it for the day.
     */
    protected void logCertificateRevocationWarning() {
        List<eu.domibus.common.model.certificate.Certificate> unNotifiedSoonRevoked = certificateDao.getUnNotifiedSoonRevoked();
        for (eu.domibus.common.model.certificate.Certificate certificate : unNotifiedSoonRevoked) {
            LOG.securityWarn(SEC_CERTIFICATE_SOON_REVOKED, certificate.getAlias(), certificate.getNotAfter());
            certificateDao.updateRevocation(certificate);
        }

        List<eu.domibus.common.model.certificate.Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedRevoked();
        for (eu.domibus.common.model.certificate.Certificate certificate : unNotifiedRevoked) {
            LOG.securityError(SEC_CERTIFICATE_REVOKED, certificate.getAlias(), certificate.getNotAfter());
            certificateDao.updateRevocation(certificate);
        }
    }

    /**
     * Group keystore and trustStore certificates in a list.
     *
     * @return a list of certificate.
     */
    protected List<eu.domibus.common.model.certificate.Certificate> groupAllKeystoreCertificates(KeyStore trustStore, KeyStore keyStore) {
        List<eu.domibus.common.model.certificate.Certificate> allCertificates = new ArrayList<>();
        allCertificates.addAll(loadAndEnrichCertificateFromKeystore(trustStore, CertificateType.PUBLIC));
        allCertificates.addAll(loadAndEnrichCertificateFromKeystore(keyStore, CertificateType.PRIVATE));
        return Collections.unmodifiableList(allCertificates);
    }

    /**
     * Load certificate from a keystore and enrich them with status and type.
     *
     * @param keyStore        the store where to retrieve the certificates.
     * @param certificateType the type of the certificate (Public/Private)
     * @return the list of certificates.
     */
    private List<eu.domibus.common.model.certificate.Certificate> loadAndEnrichCertificateFromKeystore(KeyStore keyStore, CertificateType certificateType) {
        List<eu.domibus.common.model.certificate.Certificate> certificates = new ArrayList<>();
        if (keyStore != null) {
            certificates = extractCertificateFromKeyStore(
                    keyStore);
            for (eu.domibus.common.model.certificate.Certificate certificate : certificates) {
                certificate.setCertificateType(certificateType);
                CertificateStatus certificateStatus = getCertificateStatus(certificate.getNotAfter());
                certificate.setCertificateStatus(certificateStatus);
            }
        }
        return certificates;
    }

    /**
     * Process the certificate status base on its expiration date.
     *
     * @param notAfter the expiration date of the certificate.
     * @return the certificate status.
     */
    protected CertificateStatus getCertificateStatus(Date notAfter) {
        int revocationOffsetInDays = Integer.valueOf(REVOCATION_TRIGGER_OFFSET_DEFAULT_VALUE);
        try {
            revocationOffsetInDays = Integer.valueOf(domibusPropertyProvider.getProperty(REVOCATION_TRIGGER_OFFSET_PROPERTY, REVOCATION_TRIGGER_OFFSET_DEFAULT_VALUE));
        } catch (NumberFormatException n) {

        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime offsetDate = now.plusDays(revocationOffsetInDays);
        LocalDateTime certificateEnd = LocalDateTime.fromDateFields(notAfter);
        LOG.debug("Current date[{}], offset date[{}], certificate end date:[{}]", now, offsetDate, certificateEnd);
        if (now.isAfter(certificateEnd)) {
            return CertificateStatus.REVOKED;
        } else if (offsetDate.isAfter(certificateEnd)) {
            return CertificateStatus.SOON_REVOKED;
        }
        return CertificateStatus.OK;
    }

    protected List<eu.domibus.common.model.certificate.Certificate> extractCertificateFromKeyStore(KeyStore trustStore) {
        List<eu.domibus.common.model.certificate.Certificate> certificates = new ArrayList<>();
        try {
            final Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                final X509Certificate x509Certificate = (X509Certificate) trustStore.getCertificate(alias);
                eu.domibus.common.model.certificate.Certificate certificate = new eu.domibus.common.model.certificate.Certificate();
                certificate.setAlias(alias);
                certificate.setNotAfter(x509Certificate.getNotAfter());
                certificate.setNotBefore(x509Certificate.getNotBefore());
                certificates.add(certificate);
            }
        } catch (KeyStoreException e) {
            LOG.warn(e.getMessage(), e);
        }
        return Collections.unmodifiableList(certificates);
    }


    public X509Certificate loadCertificateFromString(String content) {
        CertificateFactory certFactory = null;
        X509Certificate cert = null;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        try {
            cert = (X509Certificate) certFactory.generateCertificate(in);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return cert;
    }

    public TrustStoreEntry convertCertificateContent(String certificateContent) {
        X509Certificate cert = loadCertificateFromString(certificateContent);
        TrustStoreEntry res = createTrustStoreEntry(cert);
        return res;
    }

    public TrustStoreEntry getPartyCertificateFromTruststore(String partyName) throws KeyStoreException {
        X509Certificate cert = multiDomainCertificateProvider.getCertificateFromTruststore(domainProvider.getCurrentDomain(), partyName);
        LOG.debug("get certificate from truststore for [{}] = [{}] ", partyName, cert);
        TrustStoreEntry res = createTrustStoreEntry(cert);
        if (res != null)
            res.setFingerprints(extractFingerprints(cert));
        return res;
    }

    private TrustStoreEntry createTrustStoreEntry(X509Certificate certificate) {
        return createTrustStoreEntry(null, certificate);
    }

    private TrustStoreEntry createTrustStoreEntry(String alias, X509Certificate certificate) {
        if (certificate == null)
            return null;
        return new TrustStoreEntry(
                alias,
                certificate.getSubjectDN().getName(),
                certificate.getIssuerDN().getName(),
                certificate.getNotBefore(),
                certificate.getNotAfter());
    }

    private String extractFingerprints(final X509Certificate certificate) {
        if (certificate == null)
            return null;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] der = new byte[0];
        try {
            der = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        md.update(der);
        byte[] digest = md.digest();
        String digestHex = DatatypeConverter.printHexBinary(digest);
        return digestHex.toLowerCase();
    }

}



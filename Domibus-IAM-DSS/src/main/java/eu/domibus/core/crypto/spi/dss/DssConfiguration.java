package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.x509.KeyStoreCertificateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.io.IOException;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
@PropertySource(value = "classpath:iam-dss.properties")
@PropertySource(ignoreResourceNotFound=true,value = "file:${domibus.config.location}/extensions/config/iam-dss.properties")
public class DssConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DssConfiguration.class);

    @Value("${domibus.oj.content.keystore.type}")
    private String keystoreType;

    @Value("${domibus.oj.content.keystore.path}")
    private String keystorePath;

    @Value("${domibus.oj.content.keystore.password}")
    private String keystorePassword;

    @Value("${domibus.current.oj.url}")
    private String currentOjUrl;

    @Value("${domibus.current.lotl.url}")
    private String currentLotlUrl;

    @Value("${domibus.lotl.country.code}")
    private String lotlCountryCode;

    @Value("${domibus.lotl.root.scheme.info.uri}")
    private String lotlSchemeUri;

    @Value("${domibus.dss.cache.path}")
    private String dssCachePath;

    @Bean
    public TrustedListsCertificateSource trustedListSource() {
        return new TrustedListsCertificateSource();
    }

    @Bean
    public TSLRepository tslRepository(TrustedListsCertificateSource trustedListSource) {
        LOG.info("Dss trust list cache path:[{}]",dssCachePath);
        TSLRepository tslRepository = new TSLRepository();
        tslRepository.setTrustedListsCertificateSource(trustedListSource);
        tslRepository.setCacheDirectoryPath(dssCachePath);
        return tslRepository;
    }

    @Bean
    public CertificateVerifier certificateVerifier() throws Exception {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setTrustedCertSource(trustedListSource());
        /*certificateVerifier.setCrlSource(cachedCRLSource());
        certificateVerifier.setOcspSource(ocspSource());
        certificateVerifier.setDataLoader(dataLoader());*/

        // Default configs
        certificateVerifier.setExceptionOnMissingRevocationData(true);
        certificateVerifier.setCheckRevocationForUntrustedChains(false);

        return certificateVerifier;
    }

    @Bean
    public KeyStoreCertificateSource ojContentKeyStore() throws IOException {
        LOG.info("Initializing DSS trust list trustStore:");
        LOG.info("  trustStore type:[{}]", keystoreType);
        LOG.info("  trustStore path:[{}]", keystorePassword);
        return new KeyStoreCertificateSource(new File(keystorePath), keystoreType, keystorePassword);
    }

    @Bean
    public DomibusDataLoader dataLoader() {
        DomibusDataLoader dataLoader = new DomibusDataLoader();
        dataLoader.setProxyConfig(null);
        return dataLoader;
    }

    @Bean
    public TSLValidationJob tslValidationJob(DataLoader dataLoader, TSLRepository tslRepository, KeyStoreCertificateSource ojContentKeyStore) {
        LOG.info("Dss lotl url:[{}]",currentLotlUrl);
        LOG.info("Dss lotl schema uri:[{}]",lotlSchemeUri);
        LOG.info("Dss lotl country code:[{}]",lotlCountryCode);
        LOG.info("Dss oj url:[{}]",currentOjUrl);
        TSLValidationJob validationJob = new TSLValidationJob();
        validationJob.setDataLoader(dataLoader);
        validationJob.setRepository(tslRepository);
        validationJob.setLotlUrl(currentLotlUrl);
        validationJob.setLotlRootSchemeInfoUri(lotlSchemeUri);
        validationJob.setLotlCode(lotlCountryCode);
        validationJob.setOjUrl(currentOjUrl);
        validationJob.setOjContentKeyStore(ojContentKeyStore);
        validationJob.setCheckLOTLSignature(true);
        validationJob.setCheckTSLSignatures(true);
        validationJob.initRepository();
        validationJob.refresh();
        return validationJob;
    }
}

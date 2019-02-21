package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.tsl.OtherTrustedList;
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
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
@PropertySource(value = "classpath:authentication-dss-extension.properties")
@PropertySource(ignoreResourceNotFound = true, value = "file:${domibus.config.location}/extensions/config/authentication-dss-extension.properties")
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

    private static final String CUSTOM_TRUSTED_LIST_URL_PROPERTY = "domibus.dss.custom.trusted.list.url[%s]";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_PROPERTY = "domibus.dss.custom.trusted.list.keystore.type[%s]";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_PROPERTY = "domibus.dss.custom.trusted.list.keystore.path[%s]";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_PROPERTY = "domibus.dss.custom.trusted.list.keystore.password[%s]";

    private static final String CUSTOM_TRUSTED_LIST_COUNTRY_CODE_PROPERTY = "domibus.dss.custom.trusted.list.country.code[%s]";

    @Bean
    public TrustedListsCertificateSource trustedListSource() {
        return new TrustedListsCertificateSource();
    }

    @Bean
    public TSLRepository tslRepository(TrustedListsCertificateSource trustedListSource) {
        LOG.info("Dss trust list cache path:[{}]", dssCachePath);
        TSLRepository tslRepository = new TSLRepository();
        tslRepository.setTrustedListsCertificateSource(trustedListSource);
        tslRepository.setCacheDirectoryPath(dssCachePath);
        return tslRepository;
    }

    @Bean
    public CertificateVerifier certificateVerifier() {
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
    List<OtherTrustedList> otherTrustedLists(Environment env) {
        int count = 0;
        boolean propertyExist;
        List<OtherTrustedList> customLists = new ArrayList<>();
        do {
            final String customListUrlPropertyKey = String.format(CUSTOM_TRUSTED_LIST_URL_PROPERTY, count);
            propertyExist = env.containsProperty(customListUrlPropertyKey);
            LOG.debug("Property for key:[{}] exist:[{}]", customListUrlPropertyKey, propertyExist);
            if (propertyExist) {
                final String customListKeystorePathPropertyKey = String.format(CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_PROPERTY, count);
                LOG.trace("customListKeystorePathPropertyKey:[{}]", customListKeystorePathPropertyKey);

                final String customListKeystoreTypePropertyKey = String.format(CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_PROPERTY, count);
                LOG.trace("customListKeystoreTypePropertyKey:[{}]", customListKeystoreTypePropertyKey);

                final String customListKeystorePasswordPropertyKey = String.format(CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_PROPERTY, count);
                LOG.trace("customListKeystorePasswordPropertyKey:[{}]", customListKeystorePasswordPropertyKey);

                final String customListCountryCodePropertyKey = String.format(CUSTOM_TRUSTED_LIST_COUNTRY_CODE_PROPERTY, count);
                LOG.trace("customListCountryCodePropertyKey:[{}]", customListCountryCodePropertyKey);

                final String customListUrl = env.getProperty(customListUrlPropertyKey);
                LOG.debug("Custom list:[{}] url:[{}]", count, customListUrl);

                final String customListKeystorePath = env.getProperty(customListKeystorePathPropertyKey);
                LOG.debug("Custom list:[{}] keystore path:[{}]", count, customListKeystorePath);

                final String customListKeystoreType = env.getProperty(customListKeystoreTypePropertyKey);
                LOG.debug("Custom list:[{}] keystore type:[{}]", count, customListKeystoreType);

                final String customListCountryCode = env.getProperty(customListCountryCodePropertyKey);
                LOG.debug("Custom list:[{}] country code:[{}]", count, customListCountryCode);

                final String customListKeystorePassword = env.getProperty(customListKeystorePasswordPropertyKey);

                OtherTrustedList otherTrustedList = new OtherTrustedList();
                try {
                    otherTrustedList.setTrustStore(
                            new KeyStoreCertificateSource(new File(customListKeystorePath), customListKeystoreType, customListKeystorePassword));
                    otherTrustedList.setUrl(customListUrl);
                    otherTrustedList.setCountryCode(customListCountryCode);
                    customLists.add(otherTrustedList);
                } catch (IOException e) {
                    LOG.error("Error while loading custom trust list", e);
                }
            }
            count++;
        } while (propertyExist);

        return customLists;
    }

    @Bean
    public TSLValidationJob tslValidationJob(DataLoader dataLoader, TSLRepository tslRepository, KeyStoreCertificateSource ojContentKeyStore, List<OtherTrustedList> otherTrustedLists) {
        LOG.info("Dss lotl url:[{}]", currentLotlUrl);
        LOG.info("Dss lotl schema uri:[{}]", lotlSchemeUri);
        LOG.info("Dss lotl country code:[{}]", lotlCountryCode);
        LOG.info("Dss oj url:[{}]", currentOjUrl);
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
        validationJob.setOtherTrustedLists(otherTrustedLists);
        validationJob.refresh();
        return validationJob;
    }
}

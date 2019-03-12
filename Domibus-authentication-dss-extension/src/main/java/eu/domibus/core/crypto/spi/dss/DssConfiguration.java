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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Load dss beans.
 */
@Configuration("dssConfiguration")
@PropertySource(value = "classpath:authentication-dss-extension-default.properties")
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

    @Value("${domibus.dss.proxy.https.host}")
    private String httpsHost;

    @Value("${domibus.dss.proxy.https.port}")
    private String httpsPort;

    @Value("${domibus.dss.proxy.https.user}")
    private String httpsUser;

    @Value("${domibus.dss.proxy.https.password}")
    private String httpsPassword;

    @Value("${domibus.dss.proxy.https.excludedHosts}")
    private String httpsExcludesHosts;

    @Value("${domibus.dss.proxy.http.host}")
    private String httpHost;

    @Value("${domibus.dss.proxy.http.port}")
    private String httpPort;

    @Value("${domibus.dss.proxy.http.user}")
    private String httpUser;

    @Value("${domibus.dss.proxy.http.password}")
    private String httpPassword;

    @Value("${domibus.dss.proxy.http.excludedHosts}")
    private String httpExcludedHosts;

    @Value("${domibus.dss.refresh.cron}")
    private String dssRefreshCronExpression;

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
    public CertificateVerifier certificateVerifier(DomibusDataLoader dataLoader) {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setTrustedCertSource(trustedListSource());
        certificateVerifier.setDataLoader(dataLoader);
        /*certificateVerifier.setCrlSource(cachedCRLSource());
        certificateVerifier.setOcspSource(ocspSource());
        certificateVerifier.setDataLoader(dataLoader());*/

        // Default configs
        certificateVerifier.setExceptionOnMissingRevocationData(false);
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
        return new OtherTrustedListPropertyMapper(env).map();
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
        validationJob.setOtherTrustedLists(otherTrustedLists);
        validationJob.initRepository();
        validationJob.refresh();
        return validationJob;
    }

   /* private void checkCacheConfigAndInitialize(TSLValidationJob validationJob) {
        final File file = new File(dssCachePath);
        if (!file.exists()) {
            LOG.warn("Dss cache directory:[{}] does not exist, trusted lists can not be loaded", dssCachePath);
            return;
        }
        final String[] cacheFiles = file.list();
        if (cacheFiles == null || cacheFiles.length == 0) {
            LOG.info("Dss cache directory:[{}] is empty at start up, loading trusted lists.", dssCachePath);
            validationJob.refresh();
        } else {
            LOG.info("Dss cache directory:[{}] contains:[{}] file", dssCachePath, cacheFiles.length);
        }
    }*/

    @Bean
    public JobDetailFactoryBean dssRefreshJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(DssRefreshWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean dssRefreshTrigger() {
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(dssRefreshJob().getObject());
        obj.setCronExpression(dssRefreshCronExpression);
        LOG.debug("dssRefreshTrigger configured with cronExpression [{}]", dssRefreshCronExpression);
        obj.setStartDelay(20000);
        return obj;
    }

    @Bean
    public ValidationReport validationReport(Environment env) {
        final List<ConstraintInternal> constraints = new ConstraintPropertyMapper(env).map();
        return new ValidationReport(constraints);
    }
}

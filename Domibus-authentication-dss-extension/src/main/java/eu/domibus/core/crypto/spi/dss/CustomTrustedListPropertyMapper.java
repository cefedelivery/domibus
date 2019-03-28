package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.tsl.OtherTrustedList;
import eu.europa.esig.dss.x509.KeyStoreCertificateSource;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @see eu.europa.esig.dss.tsl.OtherTrustedList
 * <p>
 * domibus.dss.custom.trusted.list.url[0]=
 * domibus.dss.custom.trusted.list.keystore.path[0]=
 * domibus.dss.custom.trusted.list.keystore.type[0]=
 * domibus.dss.custom.trusted.list.keystore.password[0]=
 * domibus.dss.custom.trusted.list.country.code[0]=
 * <p>
 * domibus.dss.custom.trusted.list.url[1]=
 * domibus.dss.custom.trusted.list.keystore.path[1]=
 * domibus.dss.custom.trusted.list.keystore.type[1]=
 * domibus.dss.custom.trusted.list.keystore.password[1]=
 * domibus.dss.custom.trusted.list.country.code[1]=
 * @since 4.1
 * <p>
 * Load multiple OtherTrustedList objects based on properties with the following format:
 */
@Component
public class CustomTrustedListPropertyMapper extends PropertyGroupMapper<OtherTrustedList> {

    private static final Logger LOG = LoggerFactory.getLogger(CustomTrustedListPropertyMapper.class);

    private static final String CUSTOM_TRUSTED_LIST_URL_PROPERTY = "domibus.dss.custom.trusted.list.url";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_PROPERTY = "domibus.dss.custom.trusted.list.keystore.type";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_PROPERTY = "domibus.dss.custom.trusted.list.keystore.path";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_PROPERTY = "domibus.dss.custom.trusted.list.keystore.password";

    private static final String CUSTOM_TRUSTED_LIST_COUNTRY_CODE_PROPERTY = "domibus.dss.custom.trusted.list.country.code";

    public CustomTrustedListPropertyMapper(final DomibusPropertyExtService domibusPropertyExtService,
                                           final DomainContextExtService domainContextExtService,
                                           final Environment environment) {
        super(domibusPropertyExtService,
                domainContextExtService, environment);
    }

    public List<OtherTrustedList> map() {
        return super.map(
                CUSTOM_TRUSTED_LIST_URL_PROPERTY,
                CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_PROPERTY,
                CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_PROPERTY,
                CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_PROPERTY,
                CUSTOM_TRUSTED_LIST_COUNTRY_CODE_PROPERTY
        );
    }

    @Override
    OtherTrustedList transform(Map<String, ImmutablePair<String, String>> keyValues) {
        OtherTrustedList otherTrustedList = new OtherTrustedList();
        String customListKeystorePath = keyValues.get(CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_PROPERTY).getRight();
        String customListKeystoreType = keyValues.get(CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_PROPERTY).getRight();
        String customListKeystorePassword = keyValues.get(CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_PROPERTY).getRight();
        String customListUrl = keyValues.get(CUSTOM_TRUSTED_LIST_URL_PROPERTY).getRight();
        String customListCountryCode = keyValues.get(CUSTOM_TRUSTED_LIST_COUNTRY_CODE_PROPERTY).getRight();
        try {
            otherTrustedList.setTrustStore(
                    new KeyStoreCertificateSource(new File(customListKeystorePath), customListKeystoreType, customListKeystorePassword));
            otherTrustedList.setUrl(customListUrl);
            otherTrustedList.setCountryCode(customListCountryCode);
            LOG.debug("Custom trusted list with keystore path:[{}] and type:[{}], URL:[{}], customListCountryCode:[{}] will be added to DSS", customListKeystorePath, customListKeystoreType, customListUrl, customListCountryCode);
            return otherTrustedList;
        } catch (IOException e) {
            LOG.error("Error while configuring custom trusted list with keystore path:[{}],type:[{}] ", customListKeystorePath, customListKeystoreType, e);
            return null;
        }
    }
}

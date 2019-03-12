package eu.domibus.core.crypto.spi.dss;

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
 * @since 4.0
 */
@Component
public class OtherTrustedListPropertyMapper extends PropertyGroupMapper<OtherTrustedList> {

    private static final Logger LOG = LoggerFactory.getLogger(OtherTrustedListPropertyMapper.class);

    private static final String CUSTOM_TRUSTED_LIST_URL_PROPERTY = "domibus.dss.custom.trusted.list.url";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_PROPERTY = "domibus.dss.custom.trusted.list.keystore.type";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_PROPERTY = "domibus.dss.custom.trusted.list.keystore.path";

    private static final String CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_PROPERTY = "domibus.dss.custom.trusted.list.keystore.password";

    private static final String CUSTOM_TRUSTED_LIST_COUNTRY_CODE_PROPERTY = "domibus.dss.custom.trusted.list.country.code";

    private Environment env;

    public OtherTrustedListPropertyMapper(Environment env) {
        this.env = env;
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
    OtherTrustedList transForm(Map<String, ImmutablePair<String, String>> keyValues) {
        OtherTrustedList otherTrustedList = new OtherTrustedList();
        try {
            String customListKeystorePath = keyValues.get(CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_PROPERTY).getRight();
            String customListKeystoreType = keyValues.get(CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_PROPERTY).getRight();
            String customListKeystorePassword = keyValues.get(CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_PROPERTY).getRight();
            String customListUrl = keyValues.get(CUSTOM_TRUSTED_LIST_URL_PROPERTY).getRight();
            String customListCountryCode = keyValues.get(CUSTOM_TRUSTED_LIST_COUNTRY_CODE_PROPERTY).getRight();

            otherTrustedList.setTrustStore(
                    new KeyStoreCertificateSource(new File(customListKeystorePath), customListKeystoreType, customListKeystorePassword));
            otherTrustedList.setUrl(customListUrl);
            otherTrustedList.setCountryCode(customListCountryCode);
        } catch (IOException e) {
            LOG.error("Error while loading custom trust list", e);
        }
        return otherTrustedList;
    }

    @Override
    Environment getEnvironment() {
        return env;
    }
}

/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.wss4j.common.crypto;

import eu.domibus.common.exception.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service(value = "truststoreService")
@Scope(value = "singleton")
public class TruststoreService {

    private static final Log LOG = LogFactory.getLog(TruststoreService.class);

    @Resource(name = "truststoreProperties")
    private Properties truststoreProperties;

    private KeyStore truststore;

    public synchronized KeyStore getTruststore() {
        if (truststore == null) {
            try {
                initTruststore();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
                LOG.error("Error while initializing truststore", e);
            }
        }
        return truststore;
    }

    /**
     * Adds the certificate to the truststore
     *
     * @param certificate the certificate to add
     * @param alias       the certifictae alias
     * @param overwrite   if {@value true} existing entries will be replaced
     * @return {@value true} if added, else {@value false}
     */
    public boolean addCertificate(final X509Certificate certificate, final String alias, final boolean overwrite) {
        boolean containsAlias = false;
        try {
            containsAlias = getTruststore().containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new RuntimeException("This should never happen", e);
        }
        if (containsAlias && !overwrite) {
            return false;
        }
        try {
            if (containsAlias) {
                getTruststore().deleteEntry(alias);
            }
            getTruststore().setCertificateEntry(alias, certificate);

            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    private void initTruststore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        ks.load(new FileInputStream(truststoreProperties.getProperty("org.apache.ws.security.crypto.merlin.truststore.file")), truststoreProperties.getProperty("org.apache.ws.security.crypto.merlin.truststore.password").toCharArray());
        truststore = ks;
    }


}

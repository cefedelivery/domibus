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

import eu.domibus.spring.SpringContextProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wss4j.common.crypto.PasswordEncryptor;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Configurable
public class Merlin extends org.apache.wss4j.common.crypto.Merlin {

    private static final Log LOG = LogFactory.getLog(Merlin.class);

    private CryptoService cryptoService;

    public Merlin(final Properties properties, final ClassLoader loader, final PasswordEncryptor passwordEncryptor) throws WSSecurityException, IOException {
        super(properties, loader, passwordEncryptor);
        cryptoService = (CryptoService) SpringContextProvider.getApplicationContext().getBean("cryptoService");
        setTrustStore(cryptoService.getTrustStore());
        cryptoService.setCrypto(this);
        LOG.debug("Merlin crypto successfully initialized");
    }

}

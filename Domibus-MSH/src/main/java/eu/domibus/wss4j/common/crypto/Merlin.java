
package eu.domibus.wss4j.common.crypto;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.spring.SpringContextProvider;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(Merlin.class);

    private CryptoService cryptoService;

    public Merlin(final Properties properties, final ClassLoader loader, final PasswordEncryptor passwordEncryptor) throws WSSecurityException, IOException {
        super(properties, loader, passwordEncryptor);
        cryptoService = (CryptoService) SpringContextProvider.getApplicationContext().getBean("cryptoService");
        setTrustStore(cryptoService.getTrustStore());
        LOG.debug("Merlin crypto successfully initialized");
    }

}

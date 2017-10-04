package eu.domibus.jms.weblogic;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;
import weblogic.security.internal.SerializedSystemIni;
import weblogic.security.internal.encryption.ClearOrEncryptedService;
import weblogic.security.internal.encryption.EncryptionService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class SecurityHelper {

    private static final String SERVER_NAME_PROPERTY = "weblogic.Name";
    private static final String BOOT_IDENTITY_FILE_PROPERTY = "weblogic.system.BootIdentityFile";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityHelper.class);

    public Map<String, String> getBootIdentity() {
        Map<String, String> bootIdentity = new HashMap<String, String>();
        String servername = System.getProperty(SERVER_NAME_PROPERTY);
        String username = System.getProperty("weblogic.management.username");
        String password = System.getProperty("weblogic.management.password");
        if (username != null && password != null) {
            bootIdentity.put("username", username);
            bootIdentity.put("password", password);
            return bootIdentity;
        } else {
            String bootIdentityFile = System.getProperty(BOOT_IDENTITY_FILE_PROPERTY);
            if (bootIdentityFile == null) {
                bootIdentityFile = "servers/" + servername + "/security/boot.properties";
            }

            try {
                Configuration configuration = new PropertiesConfiguration(bootIdentityFile);
                Iterator<String> keys = configuration.getKeys();
                while (keys.hasNext()) {
                    final String name = keys.next();
                    String value = configuration.getString(name);
                    if (value.contains("{") && value.contains("}")) { // encrypted?
                        value = decrypt(value);
                    }
                    bootIdentity.put(name, value);
                }
            } catch (ConfigurationException e) {
                LOG.error("Failed to read boot identity file " + bootIdentityFile, e);
            }
        }
        return bootIdentity;
    }

    public String decrypt(String encrypted) {
        EncryptionService es = SerializedSystemIni.getEncryptionService();
        ClearOrEncryptedService ces = new ClearOrEncryptedService(es);
        String clear = ces.decrypt(encrypted);
        return clear;
    }

}

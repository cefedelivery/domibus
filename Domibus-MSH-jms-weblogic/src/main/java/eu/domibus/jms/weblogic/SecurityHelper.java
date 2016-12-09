package eu.domibus.jms.weblogic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import weblogic.security.internal.SerializedSystemIni;
import weblogic.security.internal.encryption.ClearOrEncryptedService;
import weblogic.security.internal.encryption.EncryptionService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SecurityHelper {

	private static final String SERVER_NAME_PROPERTY = "weblogic.Name";
	private static final String BOOT_IDENTITY_FILE_PROPERTY = "weblogic.system.BootIdentityFile";

	private static final Logger LOG = LoggerFactory.getLogger(SecurityHelper.class);

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
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(bootIdentityFile));
				String line = null;
				while ((line = br.readLine()) != null) {
					if (line.contains("=")) {
						String name = line.substring(0, line.indexOf("="));
						String value = line.substring(line.indexOf("=") + 1);
						if (value.contains("{") && value.contains("}")) { // encrypted?
							value = decrypt(value);
						}
						bootIdentity.put(name, value);
					}
				}
			} catch (IOException e) {
				LOG.error("Failed to read boot identity file " + bootIdentityFile, e);
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (IOException e) {
					LOG.error("Failed to close boot identity file", e);
				}
			}
		}
		return bootIdentity;
	}

	public String encrypt(String clear) {
		EncryptionService es = SerializedSystemIni.getEncryptionService();
		ClearOrEncryptedService ces = new ClearOrEncryptedService(es);
		String encrypted = ces.encrypt(clear);
		return encrypted;
	}

	public String decrypt(String encrypted) {
		EncryptionService es = SerializedSystemIni.getEncryptionService();
		ClearOrEncryptedService ces = new ClearOrEncryptedService(es);
		String clear = ces.decrypt(encrypted);
		return clear;
	}

}

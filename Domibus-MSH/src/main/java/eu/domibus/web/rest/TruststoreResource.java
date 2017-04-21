package eu.domibus.web.rest;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.wss4j.common.crypto.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Mircea Musat
 * @since 3.3
 */
@RestController
public class TruststoreResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResource.class);

    @Autowired
    private CryptoService cryptoService;

    @RequestMapping(value = "/rest/truststore", method = RequestMethod.POST)
    public ResponseEntity<String> uploadTruststoreFile(@RequestPart("truststore") MultipartFile truststore, @RequestParam("password") String password) {

        if (!truststore.isEmpty()) {
            try {
                byte[] bytes = truststore.getBytes();
                cryptoService.replaceTruststore(bytes, password);
                return ResponseEntity.ok("Truststore file has been successfully replaced.");
            } catch (Exception e) {
                LOG.error("Failed to upload the truststore file", e);
                return ResponseEntity.badRequest().body("Failed to upload the truststore file due to => " + e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Failed to upload the truststore file since it was empty.");
        }
    }
}

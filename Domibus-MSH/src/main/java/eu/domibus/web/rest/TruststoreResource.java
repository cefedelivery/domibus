package eu.domibus.web.rest;

import eu.domibus.wss4j.common.crypto.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Mircea Musat
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/truststore")
public class TruststoreResource {

    private static final Logger LOG = LoggerFactory.getLogger(TruststoreResource.class);

    @Autowired
    private CryptoService cryptoService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public
    @ResponseBody
    String uploadTruststoreFile(@RequestParam("truststore") MultipartFile truststore, @RequestParam("password") String password) {

        if (!truststore.isEmpty()) {
            try {
                byte[] bytes = truststore.getBytes();
                cryptoService.replaceTruststore(bytes, password);
                return "Truststore file has been successfully replaced.";
            } catch (Exception e) {
                LOG.error("Failed to upload the truststore file", e);
                return "Failed to upload the truststore file due to => " + e.getMessage();
            }
        } else {
            return "Failed to upload the truststore file since it was empty.";
        }
    }
}

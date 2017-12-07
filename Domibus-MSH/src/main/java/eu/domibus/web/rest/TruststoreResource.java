package eu.domibus.web.rest;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.CsvService;
import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import eu.domibus.wss4j.common.crypto.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Mircea Musat
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/truststore")
public class TruststoreResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResource.class);

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    @Qualifier("csvServiceImpl")
    private CsvService csvService;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<String> uploadTruststoreFile(@RequestPart("truststore") MultipartFile truststore, @RequestParam("password") String password) {

        if (!truststore.isEmpty()) {
            try {
                byte[] bytes = truststore.getBytes();
                cryptoService.replaceTruststore(bytes, password);
                domibusCacheService.clearCache("certValidationByAlias");
                return ResponseEntity.ok("Truststore file has been successfully replaced.");
            } catch (Exception e) {
                LOG.error("Failed to upload the truststore file", e);
                return ResponseEntity.badRequest().body("Failed to upload the truststore file due to => " + e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Failed to upload the truststore file since it was empty.");
        }
    }

    @RequestMapping(value = {"/list"}, method = GET)
    public List<TrustStoreRO> trustStoreEntries() {
        return domainConverter.convert(certificateService.getTrustStoreEntries(), TrustStoreRO.class);
    }

    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv() {
        String resultText;
        final List<TrustStoreRO> trustStoreROS = trustStoreEntries();

        try {
            resultText = csvService.exportToCSV(trustStoreROS);
        } catch (EbMS3Exception e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/ms-excel"))
                .header("Content-Disposition", "attachment; filename=truststore_datatable.csv")
                .body(resultText);
    }

}

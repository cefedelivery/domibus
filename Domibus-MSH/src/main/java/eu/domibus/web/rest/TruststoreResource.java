package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.csv.CsvCustomColumns;
import eu.domibus.core.csv.CsvExcludedItems;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyStore;
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
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private CsvServiceImpl csvServiceImpl;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<String> uploadTruststoreFile(@RequestPart("truststore") MultipartFile truststore, @RequestParam("password") String password) {

        if (!truststore.isEmpty()) {
            try {
                byte[] bytes = truststore.getBytes();
                multiDomainCertificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), bytes, password);
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
        final KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        return domainConverter.convert(certificateService.getTrustStoreEntries(trustStore), TrustStoreRO.class);
    }

    /**
     * This method returns a CSV file with the contents of Truststore table
     *
     * @return CSV file with the contents of Truststore table
     */
    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv() {
        String resultText;
        final List<TrustStoreRO> trustStoreROS = trustStoreEntries();

        try {
            resultText = csvServiceImpl.exportToCSV(trustStoreROS, TrustStoreRO.class,
                    CsvCustomColumns.TRUSTSTORE_RESOURCE.getCustomColumns(), CsvExcludedItems.TRUSTSTORE_RESOURCE.getExcludedItems());
        } catch (CsvException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + csvServiceImpl.getCsvFilename("truststore"))
                .body(resultText);
    }

}

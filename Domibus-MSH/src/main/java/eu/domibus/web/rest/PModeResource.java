package eu.domibus.web.rest;

import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Mircea Musat
 * @since 3.3
 */
@RestController
public class PModeResource {

    private static final Logger LOG = LoggerFactory.getLogger(PModeResource.class);

    @Autowired
    private PModeProvider pModeProvider;

    @RequestMapping(value = "/rest/pmode", method = RequestMethod.GET)
    public String downloadPmodes() {
        return "pmodetest";
    }

    @RequestMapping(value = "/rest/pmode", method = RequestMethod.POST)
    public ResponseEntity<String> uploadPmodes(@RequestPart("file") MultipartFile pmode) {
        if (pmode.isEmpty()) {
            return ResponseEntity.badRequest().body("Failed to upload the PMode file since it was empty.");
        }
        try {
            byte[] bytes = pmode.getBytes();

            List<String> pmodeUpdateMessage = pModeProvider.updatePModes(bytes);
            String message = "PMode file has been successfully uploaded";
            if (pmodeUpdateMessage != null && pmodeUpdateMessage.size() > 0) {
                message += " but some issues were detected: \n" + StringUtils.join(pmodeUpdateMessage, "\n");
            }
            return ResponseEntity.ok(message);
        } catch (XmlProcessingException e) {
            LOG.error("Error uploading the PMode", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the PMode file due to: \n " + e.getMessage() + "\n" + StringUtils.join(e.getErrors(), "\n"));
        } catch (Exception e) {
            LOG.error("Error uploading the PMode", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the PMode file due to: \n " + e.getMessage());
        }
    }
}

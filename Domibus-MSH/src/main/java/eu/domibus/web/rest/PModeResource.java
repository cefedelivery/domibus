package eu.domibus.web.rest;

import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Mircea Musat
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/pmode")
public class PModeResource {

    private static final Logger LOG = LoggerFactory.getLogger(PModeResource.class);

    @Autowired
    private PModeProvider pModeProvider;

    @RequestMapping(method = RequestMethod.GET)
    public String downloadPmodes() {
        return "pmodetest";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String uploadPmodes(@RequestParam("pmode") MultipartFile pmode) {
        if (pmode.isEmpty()) {
            return "Failed to upload the PMode file since it was empty.";
        }
        try {
            byte[] bytes = pmode.getBytes();

            List<String> pmodeUpdateMessage = pModeProvider.updatePModes(bytes);
            String message = "PMode file has been successfully uploaded";
            if (pmodeUpdateMessage != null && pmodeUpdateMessage.size() > 0) {
                message += " but some issues were detected: <br>" + StringUtils.join(pmodeUpdateMessage, "<br>");
            }
            return message;
        } catch (XmlProcessingException e) {
            LOG.error("Error uploading the PMode", e);
            return "Failed to upload the PMode file due to: <br><br> " + StringUtils.join(e.getErrors(), "<br>");
        } catch (Exception e) {
            LOG.error("Error uploading the PMode", e);
            return "Failed to upload the PMode file due to: <br><br> " + e.getMessage();
        }
    }
}

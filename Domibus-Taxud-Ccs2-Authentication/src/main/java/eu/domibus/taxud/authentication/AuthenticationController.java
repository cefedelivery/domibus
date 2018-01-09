package eu.domibus.taxud.authentication;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
public class AuthenticationController {

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

    @PostMapping(value = "/message", consumes = "multipart/form-data")
    public void  authenticate(  @RequestPart("submissionJson") Submission submission,
                                @RequestPart(value = "payload") MultipartFile payload) {
        LOG.info("Message received:");
        submissionLog.logAccesPoints(submission);
        try {
            byte[] decode = Base64.decodeBase64(payload.getBytes());
            LOG.info("Content:[{}]",new String(decode));
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }
    }

}

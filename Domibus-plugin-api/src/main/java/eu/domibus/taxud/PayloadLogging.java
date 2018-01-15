package eu.domibus.taxud;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class PayloadLogging {
    private final static Logger LOG = LoggerFactory.getLogger(PayloadLogging.class);

    public void decodeAndlog(byte[] payload) {
        if (LOG.isInfoEnabled()) {
            byte[] decode = Base64.decodeBase64(payload);
            LOG.info("Payload:[{}]", new String(decode));
        }
    }

    public void log(byte[] payload) {
        LOG.info("Payload:[{}]", new String(payload));
    }
}

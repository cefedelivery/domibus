package eu.domibus.taxud;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifierUtil {

    public final static String UNREGISTERED="unregistered";

    private static final Logger LOG = LoggerFactory.getLogger(IdentifierUtil.class);

    public static String[] splitIdentifier(String originalSender) {
        int startParsingIndex = originalSender.indexOf(UNREGISTERED) + UNREGISTERED.length()+1;
        String sender = originalSender.substring(startParsingIndex);
        String[] split = sender.split(":");
        if(split.length!=3){
            LOG.warn("Invalid UMDS format for original sender:[{}]",originalSender);
            throw new IllegalArgumentException("Invalid UMDS format for original sender");
        }
        return split;
    }

}

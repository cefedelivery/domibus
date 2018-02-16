package eu.domibus.taxud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifierUtil {

    public final static String UNREGISTERED="unregistered";

    public static String[] splitIdentifier(String originalSender) {
        int startParsingIndex = originalSender.indexOf(UNREGISTERED) + UNREGISTERED.length()+1;
        String sender = originalSender.substring(startParsingIndex);
        String[] split = sender.split(":");
        if(split.length!=3){
            throw new IllegalArgumentException("Invalid UMDS format for original sender");
        }
        return split;
    }

}

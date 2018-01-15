package eu.domibus.plugin.jms;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Umds;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class IdentifierHelper {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IdentifierHelper.class);

    private final static String UNREGISTERED="unregistered";

    public Umds buildUmdsFromOriginalSender(final String originalSender){
        String[] split = splitIdetifier(originalSender);
        Umds umds = new Umds();
        umds.setUser_typeOfIdentifier(split[0]);
        umds.setUser_identifier(split[1]);
        umds.setUser_typeOfActor(split[2]);
        return umds;
    }

    public void updateDelegatorInfo(final Umds umds,final String delegator){
        String[] split = splitIdetifier(delegator);
        umds.setDelegator_typeOfIdentifier(split[0]);
        umds.setDelegator_identifier(split[1]);
        umds.setDelegator_typeOfActor(split[2]);
    }

    private String[] splitIdetifier(String originalSender) {
        int startParsingIndex = originalSender.indexOf(UNREGISTERED) + UNREGISTERED.length()+1;
        String sender = originalSender.substring(startParsingIndex);
        String[] split = sender.split(":");
        if(split.length!=3){
            LOG.error("Invalid identifier [{}]",sender);
            throw new IllegalArgumentException("Invalid UMDS format for original sender");
        }
        return split;
    }

    public String getApplicationUrl(final String finalRecipient){
        int startParsingIndex = finalRecipient.indexOf(UNREGISTERED) + UNREGISTERED.length()+1;
        return finalRecipient.substring(startParsingIndex);
    }


}

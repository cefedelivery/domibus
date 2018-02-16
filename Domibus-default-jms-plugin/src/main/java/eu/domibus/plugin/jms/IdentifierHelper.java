package eu.domibus.plugin.jms;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Umds;
import eu.domibus.taxud.IdentifierUtil;
import org.springframework.stereotype.Component;

import static eu.domibus.taxud.IdentifierUtil.UNREGISTERED;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class IdentifierHelper {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IdentifierHelper.class);



    public Umds buildUmdsFromOriginalSender(final String originalSender){
        String[] split = splitIdentifier(originalSender);
        Umds umds = new Umds();
        umds.setUser_typeOfIdentifier(split[0]);
        umds.setUser_identifier(split[1]);
        umds.setUser_typeOfActor(split[2]);
        return umds;
    }

    public void updateDelegatorInfo(final Umds umds,final String delegator){
        String[] split = splitIdentifier(delegator);
        umds.setDelegator_typeOfIdentifier(split[0]);
        umds.setDelegator_identifier(split[1]);
        umds.setDelegator_typeOfActor(split[2]);
    }

    private String[] splitIdentifier(String originalSender) {
        return IdentifierUtil.splitIdentifier(originalSender);
    }

    public String getApplicationUrl(final String finalRecipient){
        int startParsingIndex = finalRecipient.indexOf(UNREGISTERED) + UNREGISTERED.length()+1;
        return finalRecipient.substring(startParsingIndex);
    }


}

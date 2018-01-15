package eu.domibus.plugin.jms;

import eu.domibus.plugin.Umds;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class IdentifierHelperTest {

    @Test
    public void buildUmds() {
        IdentifierHelper identifierHelper=new IdentifierHelper();
        Umds umds = identifierHelper.buildUmdsFromOriginalSender("urn:oasis:names:tc:ebcore:partyid-type:unregistered:EID:bourgpa:EMPL");
        assertEquals("EID",umds.getUser_typeOfIdentifier());
        assertEquals("bourgpa",umds.getUser_identifier());
        assertEquals("EMPL",umds.getUser_typeOfActor());
    }

    @Test
    public void getApplicationUrl(){
        IdentifierHelper identifierHelper=new IdentifierHelper();
        String applicationUrl = identifierHelper.getApplicationUrl("urn:oasis:names:tc:ebcore:partyid-type:unregistered:http://intragrate/folio");
        assertEquals("http://intragrate/folio",applicationUrl);

    }
}
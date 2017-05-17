package eu.domibus.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * @author idragusa
 * @since 3.2.5
 */
public class SoapUtil {
    private static final Log logger = LogFactory.getLog(SoapUtil.class);

    public static String getRawXMLMessage(SOAPMessage soapMessage) {
        String rawXMLMessage = null;
        final StringWriter sw = new StringWriter();

        try {
            TransformerFactory.newInstance().newTransformer().transform(
                    new DOMSource(soapMessage.getSOAPPart()),
                    new StreamResult(sw));
        } catch (TransformerException e) {
            logger.error("Unable to log the raw message XML due to error: ", e);
            return rawXMLMessage;
        }

        rawXMLMessage = sw.toString();
        return rawXMLMessage;
    }

}

package eu.domibus.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author idragusa
 * @since 3.2.5
 */
public class SoapUtil {
    private static final Log logger = LogFactory.getLog(SoapUtil.class);

    public static String getRawXMLMessage(SOAPMessage soapMessage) throws TransformerException {
        String rawXMLMessage = null;
        final StringWriter sw = new StringWriter();

        TransformerFactory.newInstance().newTransformer().transform(
                new DOMSource(soapMessage.getSOAPPart()),
                new StreamResult(sw));

        rawXMLMessage = sw.toString();
        return rawXMLMessage;
    }





}

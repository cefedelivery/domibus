package eu.domibus.common.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import java.net.URL;

/**
 * Created by martifp on 21/04/2016.
 */
public class XmlValidationEventHandler implements ValidationEventHandler {

    private static final Log LOG = LogFactory.getLog(XmlValidationEventHandler.class);

    public boolean handleEvent(ValidationEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("No event found!");
        }

        String severity = null;
        boolean retVal = false;
        switch (event.getSeverity()) {
            case ValidationEvent.WARNING:
                severity = "WARNING";
                retVal = true; // continue after warnings
                break;
            case ValidationEvent.ERROR:
                severity = "ERROR";
                retVal = true; // continue after errors because we want to show all errors together
                break;
            case ValidationEvent.FATAL_ERROR:
                severity = "FATAL_ERROR";
                retVal = false; // terminate after fatal errors
                break;
            default:
                assert false :
                        severity = "UNRECOGNIZED_SEVERITY";
        }

        String location = getLocation(event);

        LOG.debug("[" + severity + "] is [" + event.getMessage() + "] at [" + location + "]");

        return retVal;
    }

    /**
     * Calculates a location message for the event
     */
    private String getLocation(ValidationEvent event) {
        StringBuffer msg = new StringBuffer();

        ValidationEventLocator locator = event.getLocator();

        if (locator != null) {

            URL url = locator.getURL();
            Object obj = locator.getObject();
            Node node = locator.getNode();
            int line = locator.getLineNumber();

            if (url != null || line != -1) {
                msg.append("line " + line);
                if (url != null)
                    msg.append(" of " + url);
            } else if (obj != null) {
                msg.append(" obj: " + obj.toString());
            } else if (node != null) {
                msg.append(" node: " + node.toString());
            }
        } else {
            msg.append("LOCATION_UNAVAILABLE");
        }

        return msg.toString();
    }
}

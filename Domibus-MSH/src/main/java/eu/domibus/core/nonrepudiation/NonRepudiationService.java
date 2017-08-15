package eu.domibus.core.nonrepudiation;

import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */

public interface NonRepudiationService {

    void saveRequest(SOAPMessage request, UserMessage userMessage);

    void saveResponse(SOAPMessage response,SignalMessage signalMessage);
}

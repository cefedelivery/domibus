package eu.domibus.common.services;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.common.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface SoapService {

    Messaging getMessage(final SoapMessage message) throws IOException, JAXBException, EbMS3Exception;
}

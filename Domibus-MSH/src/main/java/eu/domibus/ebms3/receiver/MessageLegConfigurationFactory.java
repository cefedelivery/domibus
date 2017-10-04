package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface MessageLegConfigurationFactory {

    LegConfigurationExtractor extractMessageConfiguration(final SoapMessage soapMessage, final Messaging messaging);


}

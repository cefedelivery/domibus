package eu.domibus.core.nonrepudiation;

import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.SoapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class NonRepudiationDefaultService implements NonRepudiationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NonRepudiationDefaultService.class);

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Override
    public void saveRequest(SOAPMessage request, UserMessage userMessage) {
        if (isNonRepudiationAuditDisabled()) {
            return;
        }

        try {
            String rawXMLMessage = SoapUtil.getRawXMLMessage(request);
            LOG.debug("Persist raw XML envelope: " + rawXMLMessage);
            RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
            rawEnvelopeLog.setRawXML(rawXMLMessage);
            rawEnvelopeLog.setUserMessage(userMessage);
            rawEnvelopeLogDao.create(rawEnvelopeLog);
        } catch (TransformerException e) {
            LOG.warn("Unable to log the raw message XML due to: ", e);
        }
    }

    @Override
    public void saveResponse(SOAPMessage response, SignalMessage signalMessage) {
        if (isNonRepudiationAuditDisabled()) {
            return;
        }

        try {
            String rawXMLMessage = SoapUtil.getRawXMLMessage(response);
            LOG.debug("Persist raw XML envelope: " + rawXMLMessage);
            RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
            rawEnvelopeLog.setRawXML(rawXMLMessage);
            rawEnvelopeLog.setSignalMessage(signalMessage);
            rawEnvelopeLogDao.create(rawEnvelopeLog);
        } catch (TransformerException e) {
            LOG.warn("Unable to log the raw message XML due to: ", e);
        }
    }

    protected boolean isNonRepudiationAuditDisabled() {
        String nonRepudiationActive = domibusProperties.getProperty("domibus.nonrepudiation.audit.active", "true");
        return !Boolean.valueOf(nonRepudiationActive);
    }
}

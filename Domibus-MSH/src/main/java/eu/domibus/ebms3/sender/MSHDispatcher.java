
package eu.domibus.ebms3.sender;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;
import org.apache.cxf.message.Message;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Service
public class MSHDispatcher {

    public static final String MESSAGE_TYPE_IN = "MESSAGE_TYPE";
    public static final String MESSAGE_TYPE_OUT = "MESSAGE_TYPE_OUT";
    public static final String DOMIBUS_DISPATCHER_CACHEABLE = "domibus.dispatcher.cacheable";
    public static final String DOMIBUS_DISPATCHER_CACHEABLE_DEFAULT = "false";
    public static final String LOCAL_MSH_ENDPOINT = "local://localMSH";
    public static final String HEADER_DOMIBUS_MESSAGE_ID = "DOMIBUS-MESSAGE_ID";
    public static final String HEADER_DOMIBUS_SPLITTING_COMPRESSION = "DOMIBUS-SPLITTING-COMPRESSION";
    public static final String HEADER_DOMIBUS_DOMAIN = "DOMIBUS-DOMAIN";

    @Autowired
    private DispatchClientProvider dispatchClientProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Transactional(propagation = Propagation.MANDATORY)
    public SOAPMessage dispatch(final SOAPMessage soapMessage, String endpoint, final Policy policy, final LegConfiguration legConfiguration, final String pModeKey) throws EbMS3Exception {
        boolean cacheable = isDispatchClientCacheActivated();
        Domain domain = domainContextProvider.getCurrentDomain();
        final Dispatch<SOAPMessage> dispatch = dispatchClientProvider.getClient(domain.getCode(), endpoint, legConfiguration.getSecurity().getSignatureMethod().getAlgorithm(), policy, pModeKey, cacheable);

        final SOAPMessage result;
        try {
            result = dispatch.invoke(soapMessage);
        } catch (final WebServiceException e) {
            Exception exception = e;
            if(e.getCause() instanceof ConnectException) {
                exception = new WebServiceException("Error dispatching message to [" + endpoint + "]: possible reason is that the receiver is not available", e);
            }
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0005, "Error dispatching message to " + endpoint, null, exception);
            ex.setMshRole(MSHRole.SENDING);
            throw ex;
        }
        return result;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SOAPMessage dispatchLocal(final UserMessage userMessage, final SOAPMessage soapMessage, LegConfiguration legConfiguration) throws EbMS3Exception {
        Domain domain = domainContextProvider.getCurrentDomain();
        String endpoint = LOCAL_MSH_ENDPOINT;

        final Dispatch<SOAPMessage> dispatch = dispatchClientProvider.getLocalClient(domain.getCode(), endpoint);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(HEADER_DOMIBUS_MESSAGE_ID, Arrays.asList(userMessage.getMessageInfo().getMessageId()));
        headers.put(HEADER_DOMIBUS_DOMAIN, Arrays.asList(domainContextProvider.getCurrentDomain().getCode()));
        if(legConfiguration.getSplitting() != null && legConfiguration.getSplitting().getCompression()) {
            headers.put(HEADER_DOMIBUS_SPLITTING_COMPRESSION, Arrays.asList("true"));

        }
        dispatch.getRequestContext().put(Message.PROTOCOL_HEADERS, headers);

        final SOAPMessage result;
        try {
            result = dispatch.invoke(soapMessage);
        } catch (final WebServiceException e) {
            Exception exception = e;
            if(e.getCause() instanceof ConnectException) {
                exception = new WebServiceException("Error dispatching message to [" + endpoint + "]: possible reason is that the receiver is not available", e);
            }
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0005, "Error dispatching message to " + endpoint, null, exception);
            ex.setMshRole(MSHRole.SENDING);
            throw ex;
        }
        return result;
    }

    protected boolean isDispatchClientCacheActivated() {
        return domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_DISPATCHER_CACHEABLE);
    }

}


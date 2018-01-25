
package eu.domibus.ebms3.sender;

import com.codahale.metrics.Timer;
import eu.domibus.api.metrics.Metrics;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import java.net.ConnectException;
import java.util.Properties;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author Christian Koch, Stefan Mueller
 * @Since 3.0
 */
@Service
public class MSHDispatcher {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHDispatcher.class);

    public static final String MESSAGE_TYPE_IN = "MESSAGE_TYPE";
    public static final String MESSAGE_TYPE_OUT = "MESSAGE_TYPE_OUT";

    @Autowired
    private DispatchClientProvider dispatchClientProvider;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Transactional(propagation = Propagation.MANDATORY)
    public SOAPMessage dispatch(final SOAPMessage soapMessage, String endpoint, final Policy policy, final LegConfiguration legConfiguration, final String pModeKey) throws EbMS3Exception {
        boolean cacheable = isDispatchClientCacheActivated();
        final Timer.Context createDispatcherContext = Metrics.METRIC_REGISTRY.timer(name(MSHDispatcher.class, "dispatchClientProvider.getClient")).time();
        final Dispatch<SOAPMessage> dispatch = dispatchClientProvider.getClient(endpoint, legConfiguration.getSecurity().getSignatureMethod().getAlgorithm(), policy, pModeKey, cacheable);
        createDispatcherContext.stop();

        final Timer.Context invokeContext = Metrics.METRIC_REGISTRY.timer(name(MSHDispatcher.class, "dispatch.invoke")).time();
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
        } finally {
            invokeContext.stop();
        }
        return result;
    }

    protected boolean isDispatchClientCacheActivated() {
        String dispatchClientCacheable = domibusProperties.getProperty("domibus.dispatcher.cacheable", "false");
        return Boolean.valueOf(dispatchClientCacheable);
    }

}


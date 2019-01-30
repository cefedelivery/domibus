package eu.domibus.ebms3.sender;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.proxy.DomibusProxy;
import eu.domibus.proxy.DomibusProxyService;
import eu.domibus.proxy.DomibusProxyServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;
import java.util.concurrent.Executor;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class DispatchClientDefaultProvider implements DispatchClientProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DispatchClientDefaultProvider.class);

    public static final String PMODE_KEY_CONTEXT_PROPERTY = "PMODE_KEY_CONTEXT_PROPERTY";
    public static final String ASYMMETRIC_SIG_ALGO_PROPERTY = "ASYMMETRIC_SIG_ALGO_PROPERTY";
    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final QName SERVICE_NAME = new QName("http://domibus.eu", "msh-dispatch-service");
    public static final QName PORT_NAME = new QName("http://domibus.eu", "msh-dispatch");
    public static final String DOMIBUS_DISPATCHER_CONNECTIONTIMEOUT = "domibus.dispatcher.connectionTimeout";
    public static final String DOMIBUS_DISPATCHER_RECEIVETIMEOUT = "domibus.dispatcher.receiveTimeout";
    public static final String DOMIBUS_DISPATCHER_ALLOWCHUNKING = "domibus.dispatcher.allowChunking";
    public static final String DOMIBUS_DISPATCHER_CHUNKINGTHRESHOLD = "domibus.dispatcher.chunkingThreshold";
    public static final String DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE = "domibus.dispatcher.connection.keepAlive";


    @Autowired
    private TLSReader tlsReader;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor executor;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    @Qualifier("domibusProxyService")
    protected DomibusProxyService domibusProxyService;

    @Cacheable(value = "dispatchClient", key = "#domain + #endpoint + #pModeKey", condition = "#cacheable")
    @Override
    public Dispatch<SOAPMessage> getClient(String domain, String endpoint, String algorithm, Policy policy, final String pModeKey, boolean cacheable) {
        LOG.debug("Getting the dispatch client for endpoint [{}] on domain [{}]", endpoint, domain);

        final Dispatch<SOAPMessage> dispatch = createWSServiceDispatcher(endpoint);
        dispatch.getRequestContext().put(PolicyConstants.POLICY_OVERRIDE, policy);
        dispatch.getRequestContext().put(ASYMMETRIC_SIG_ALGO_PROPERTY, algorithm);
        dispatch.getRequestContext().put(PMODE_KEY_CONTEXT_PROPERTY, pModeKey);
        final Client client = ((DispatchImpl<SOAPMessage>) dispatch).getClient();
        final HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        final HTTPClientPolicy httpClientPolicy = httpConduit.getClient();

        httpConduit.setClient(httpClientPolicy);
        setHttpClientPolicy(httpClientPolicy);

        if (endpoint.startsWith("https://")) {
            final TLSClientParameters params = tlsReader.getTlsClientParameters(domain);
            if(params != null) {
                httpConduit.setTlsClientParameters(params);
            }
        }

        configureProxy(httpClientPolicy, httpConduit);
        return dispatch;
    }

    protected void setHttpClientPolicy(HTTPClientPolicy httpClientPolicy) {
        //ConnectionTimeOut - Specifies the amount of time, in milliseconds, that the consumer will attempt to establish a connection before it times out. 0 is infinite.
        int connectionTimeout = Integer.parseInt(domibusPropertyProvider.getDomainProperty(DOMIBUS_DISPATCHER_CONNECTIONTIMEOUT));
        httpClientPolicy.setConnectionTimeout(connectionTimeout);
        //ReceiveTimeOut - Specifies the amount of time, in milliseconds, that the consumer will wait for a response before it times out. 0 is infinite.
        int receiveTimeout = Integer.parseInt(domibusPropertyProvider.getDomainProperty(DOMIBUS_DISPATCHER_RECEIVETIMEOUT));
        httpClientPolicy.setReceiveTimeout(receiveTimeout);
        httpClientPolicy.setAllowChunking(Boolean.valueOf(domibusPropertyProvider.getDomainProperty(DOMIBUS_DISPATCHER_ALLOWCHUNKING)));
        httpClientPolicy.setChunkingThreshold(Integer.parseInt(domibusPropertyProvider.getDomainProperty(DOMIBUS_DISPATCHER_CHUNKINGTHRESHOLD)));

        Boolean keepAlive = Boolean.parseBoolean(domibusPropertyProvider.getDomainProperty(DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE));
        ConnectionType connectionType = ConnectionType.CLOSE;
        if(keepAlive) {
            connectionType = ConnectionType.KEEP_ALIVE;
        }
        httpClientPolicy.setConnection(connectionType);
    }

    protected Dispatch<SOAPMessage> createWSServiceDispatcher(String endpoint) {
        final javax.xml.ws.Service service = javax.xml.ws.Service.create(SERVICE_NAME);
        service.setExecutor(executor);
        service.addPort(PORT_NAME, SOAPBinding.SOAP12HTTP_BINDING, endpoint);
        final Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_NAME, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
        return dispatch;
    }

    protected void configureProxy(final HTTPClientPolicy httpClientPolicy, HTTPConduit httpConduit) {
        if(!domibusProxyService.useProxy()) {
            LOG.debug("Usage of proxy not required");
            return ;
        }

        DomibusProxy domibusProxy = domibusProxyService.getDomibusProxy();
        LOG.debug("Configuring proxy [{}] [{}] [{}] [{}] ", domibusProxy.getHttpProxyHost(),
                domibusProxy.getHttpProxyPort(), domibusProxy.getHttpProxyUser(), domibusProxy.getNonProxyHosts());
        httpClientPolicy.setProxyServer(domibusProxy.getHttpProxyHost());
        httpClientPolicy.setProxyServerPort(domibusProxy.getHttpProxyPort());
        httpClientPolicy.setProxyServerType(org.apache.cxf.transports.http.configuration.ProxyServerType.HTTP);

        if (!StringUtils.isBlank(domibusProxy.getNonProxyHosts())) {
            httpClientPolicy.setNonProxyHosts(domibusProxy.getNonProxyHosts());
        }

        if (!domibusProxyService.isProxyUserSet()) {
            ProxyAuthorizationPolicy policy = new ProxyAuthorizationPolicy();
            policy.setUserName(domibusProxy.getHttpProxyUser());
            policy.setPassword(domibusProxy.getHttpProxyPassword());
            httpConduit.setProxyAuthorization(policy);
        }
    }
}

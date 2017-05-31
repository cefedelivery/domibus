/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.ebms3.sender;

import com.google.common.base.Strings;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.DomibusCertificateException;
import eu.domibus.pki.PolicyService;
import org.apache.commons.lang.Validate;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller
 * @Since 3.0
 */
@Service
public class MSHDispatcher {


    public static final String PMODE_KEY_CONTEXT_PROPERTY = "PMODE_KEY_CONTEXT_PROPERTY";
    public static final String ASYMMETRIC_SIG_ALGO_PROPERTY = "ASYMMETRIC_SIG_ALGO_PROPERTY";
    public static final String MESSAGE_SENDER = "MESSAGE_SENDER";
    public static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    public static final QName SERVICE_NAME = new QName("http://domibus.eu", "msh-dispatch-service");
    public static final QName PORT_NAME = new QName("http://domibus.eu", "msh-dispatch");

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHDispatcher.class);

    @Autowired
    PolicyService policyService;

    @Autowired
    private TLSReader tlsReader;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    CertificateService certificateService;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Transactional(propagation = Propagation.MANDATORY)
    public SOAPMessage dispatch(final SOAPMessage soapMessage, final String pModeKey) throws EbMS3Exception {

        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        Party sendingParty = pModeProvider.getSenderParty(pModeKey);
        Validate.notNull(sendingParty, "Initiator party was not found");
        Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
        Validate.notNull(receiverParty, "Responder party was not found");
        Policy policy;
        try {
            policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
        } catch (final ConfigurationException e) {

            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Policy configuration invalid", null, e);
            ex.setMshRole(MSHRole.SENDING);
            throw ex;
        }

        if (!policyService.isNoSecurityPolicy(policy)) {
            // Verifies the validity of sender's certificate and reduces security issues due to possible hacked access points.
            try {
                certificateService.isCertificateValid(sendingParty.getName());
                LOG.info("Sender certificate exists and is valid [" + sendingParty.getName() + "]");
            } catch (DomibusCertificateException dcEx) {
                String msg = "Could not find and verify sender's certificate [" + sendingParty.getName() + "]";
                LOG.error(msg, dcEx);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, msg, null, dcEx);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

            // Verifies the validity of receiver's certificate.
            if (certificateService.isCertificateValidationEnabled()) {
                try {
                    boolean certificateChainValid = certificateService.isCertificateChainValid(receiverParty.getName());
                    if (!certificateChainValid) {
                        warnOutput("Certificate is not valid or it has been revoked [" + receiverParty.getName() + "]");
                    }
                } catch (Exception e) {
                    LOG.warn("Could not verify if the certificate chain is valid for alias " + receiverParty.getName(), e);
                }
            }
        }

        final String endpoint = receiverParty.getEndpoint();
        final Dispatch<SOAPMessage> dispatch = createWSServiceDispatcher(endpoint);//service.createDispatch(PORT_NAME, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
        dispatch.getRequestContext().put(PolicyConstants.POLICY_OVERRIDE, policy);
        dispatch.getRequestContext().put(ASYMMETRIC_SIG_ALGO_PROPERTY, legConfiguration.getSecurity().getSignatureMethod().getAlgorithm());
        dispatch.getRequestContext().put(PMODE_KEY_CONTEXT_PROPERTY, pModeKey);
        final Client client = ((DispatchImpl<SOAPMessage>) dispatch).getClient();
        final HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        final HTTPClientPolicy httpClientPolicy = httpConduit.getClient();
        httpConduit.setClient(httpClientPolicy);
        //ConnectionTimeOut - Specifies the amount of time, in milliseconds, that the consumer will attempt to establish a connection before it times out. 0 is infinite.
        int connectionTimeout = Integer.parseInt(domibusProperties.getProperty("domibus.dispatcher.connectionTimeout", "120000"));
        httpClientPolicy.setConnectionTimeout(connectionTimeout);
        //ReceiveTimeOut - Specifies the amount of time, in milliseconds, that the consumer will wait for a response before it times out. 0 is infinite.
        int receiveTimeout = Integer.parseInt(domibusProperties.getProperty("domibus.dispatcher.receiveTimeout", "120000"));
        httpClientPolicy.setReceiveTimeout(receiveTimeout);
        final TLSClientParameters params = tlsReader.getTlsClientParameters();
        if (params != null && endpoint.startsWith("https://")) {
            httpConduit.setTlsClientParameters(params);
        }
        final SOAPMessage result;

        String useProxy = domibusProperties.getProperty("domibus.proxy.enabled", "false");
        Boolean useProxyBool = Boolean.parseBoolean(useProxy);
        if (useProxyBool) {
            LOG.info("Usage of Proxy required");
            configureProxy(httpClientPolicy, httpConduit);
        } else {
            LOG.info("No proxy configured");
        }
        try {
            result = dispatch.invoke(soapMessage);
        } catch (final WebServiceException e) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0005, "error dispatching message to " + endpoint, null, e);
            ex.setMshRole(MSHRole.SENDING);
            throw ex;
        }
        return result;
    }

    protected Dispatch<SOAPMessage> createWSServiceDispatcher(String endpoint) {
        final javax.xml.ws.Service service = javax.xml.ws.Service.create(SERVICE_NAME);
        service.addPort(PORT_NAME, SOAPBinding.SOAP12HTTP_BINDING, endpoint);
        final Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_NAME, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
        return dispatch;
    }

    protected void configureProxy(final HTTPClientPolicy httpClientPolicy, HTTPConduit httpConduit) {
        String httpProxyHost = domibusProperties.getProperty("domibus.proxy.http.host");
        String httpProxyPort = domibusProperties.getProperty("domibus.proxy.http.port");
        String httpProxyUser = domibusProperties.getProperty("domibus.proxy.user");
        String httpProxyPassword = domibusProperties.getProperty("domibus.proxy.password");
        String httpNonProxyHosts = domibusProperties.getProperty("domibus.proxy.nonProxyHosts");
        if (!Strings.isNullOrEmpty(httpProxyHost) && !Strings.isNullOrEmpty(httpProxyPort)) {
            httpClientPolicy.setProxyServer(httpProxyHost);
            httpClientPolicy.setProxyServerPort(Integer.valueOf(httpProxyPort));
            httpClientPolicy.setProxyServerType(org.apache.cxf.transports.http.configuration.ProxyServerType.HTTP);
        }
        if (!Strings.isNullOrEmpty(httpProxyHost)) {
            httpClientPolicy.setNonProxyHosts(httpNonProxyHosts);
        }
        if (!Strings.isNullOrEmpty(httpProxyUser) && !Strings.isNullOrEmpty(httpProxyPassword)) {
            ProxyAuthorizationPolicy policy = new ProxyAuthorizationPolicy();
            policy.setUserName(httpProxyUser);
            policy.setPassword(httpProxyPassword);
            httpConduit.setProxyAuthorization(policy);
        }
    }

    private void warnOutput(String message) {
        LOG.warn("\n\n\n");
        LOG.warn("**************** WARNING **************** WARNING **************** WARNING **************** ");
        LOG.warn(message);
        LOG.warn("*******************************************************************************************\n\n\n");
    }
}


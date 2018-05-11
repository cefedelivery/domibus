package eu.domibus.common.services.impl;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.EndpointInfo;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to query a compliant eDelivery SMP profile based on the OASIS BDX Service Metadata Publishers
 * (SMP) to extract the required information about the unknown receiver AP.
 * The SMP Lookup is done using an SMP Client software, with the following input:
 *       The End Receiver Participant ID (C4)
 *       The Document ID
 *       The Process ID
 *
 * Upon a successful lookup, the result contains the endpoint address and also othe public
 * certificate of the receiver.
 */
@Service
@Qualifier("dynamicDiscoveryServiceOASIS")
public class DynamicDiscoveryServiceOASIS implements DynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServiceOASIS.class);
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^(?<scheme>.+?)::(?<value>.+)$");
    protected static final String URN_TYPE_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    protected static final String DEFAULT_RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";


    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Cacheable(value = "lookupInfo", key = "#receiverId + #receiverIdType + #documentId + #processId + #processIdType")
    public EndpointInfo lookupInformation(final String receiverId, final String receiverIdType,
                                          final String documentId, final String processId,
                                          final String processIdType) throws EbMS3Exception {

        LOG.info("[OASIS SMP] Do the lookup by: " + receiverId + " " + receiverIdType + " " + documentId +
                " " + processId + " " + processIdType);

        try {
            DynamicDiscovery smpClient = createDynamicDiscoveryClient();

            LOG.debug("Preparing to request the ServiceMetadata");
            final ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(receiverId, receiverIdType);
            final DocumentIdentifier documentIdentifier = createDocumentIdentifier(documentId);
            final ProcessIdentifier processIdentifier = new ProcessIdentifier(processId, processIdType);
            ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);

            LOG.debug("Get the endpoint for " + transportProfileAS4);
            final Endpoint endpoint = sm.getEndpoint(processIdentifier, new TransportProfile(transportProfileAS4));
            if (endpoint == null || endpoint.getAddress() == null || endpoint.getProcessIdentifier() == null) {
                throw new ConfigurationException("Could not fetch metadata for: " + receiverId + " " + receiverIdType + " " + documentId +
                        " " + processId + " " + processIdType + " using the AS4 Protocol " + transportProfileAS4);
            }

            return new EndpointInfo(endpoint.getAddress(), endpoint.getCertificate());

        } catch (TechnicalException exc) {
            throw new ConfigurationException("Could not fetch metadata from SMP", exc);
        }
    }

    protected DynamicDiscovery createDynamicDiscoveryClient() {
        final String smlInfo = domibusPropertyProvider.getProperty(SMLZONE_KEY);
        if (smlInfo == null) {
            throw new ConfigurationException("SML Zone missing. Configure in domibus-configuration.xml");
        }

        final String certRegex = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_CERT_REGEX);
        if(StringUtils.isEmpty(certRegex)) {
            LOG.debug("The value for property domibus.dynamicdiscovery.oasisclient.regexCertificateSubjectValidation is empty.");
        }

        LOG.debug("Load trustore for the smpClient");
        KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        try {
            DefaultProxy defaultProxy = getConfiguredProxy();

            DynamicDiscoveryBuilder dynamicDiscoveryBuilder = DynamicDiscoveryBuilder.newInstance();
            dynamicDiscoveryBuilder
                    .locator(new DefaultBDXRLocator(smlInfo))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(trustStore, certRegex)));

            if (defaultProxy != null) {
                dynamicDiscoveryBuilder
                        .fetcher(new DefaultURLFetcher(defaultProxy));
            }

            LOG.debug("Creating SMP client " + (defaultProxy != null ? "with" : "without") + " proxy.");

            return dynamicDiscoveryBuilder.build();
        } catch (TechnicalException exc) {
            throw new ConfigurationException("Could not create smp client to fetch metadata from SMP", exc);
        }
    }

    protected DocumentIdentifier createDocumentIdentifier(String documentId) throws EbMS3Exception {
        try {
            String scheme = extract(documentId, "scheme");
            String value = extract(documentId, "value");
            return new DocumentIdentifier(value, scheme);
        } catch (IllegalStateException ise) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Could not extract @scheme and @value from " + documentId, null, ise);
        }
    }

    protected String extract(String doubleColonDelimitedId, String groupName) {
        Matcher m = IDENTIFIER_PATTERN.matcher(doubleColonDelimitedId);
        m.matches();
        return m.group(groupName);
    }

    protected DefaultProxy getConfiguredProxy() throws ConnectionException {
        if (useProxy()) {
            String httpProxyHost = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            String httpProxyPort = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            String httpProxyUser = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            String httpProxyPassword = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);

            if (StringUtils.isEmpty(httpProxyHost) || StringUtils.isEmpty(httpProxyPort)
                    || StringUtils.isEmpty(httpProxyUser) || StringUtils.isEmpty(httpProxyPassword)) {

                return null;
            }

            LOG.info("Proxy configured: " + httpProxyHost + " " + httpProxyPort + " " +
                    httpProxyUser);

            return new DefaultProxy(httpProxyHost, Integer.parseInt(httpProxyPort), httpProxyUser, httpProxyPassword);
        }
        return null;
    }

    private boolean useProxy() {
        String useProxy = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_ENABLED, "false");
        if (StringUtils.isEmpty(useProxy)) {
            LOG.debug("Proxy not required. The property domibus.proxy.enabled is not configured");
            return false;
        }
        return Boolean.parseBoolean(useProxy);
    }

    @Override
    public String getPartyIdType() {
        return domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_PARTYID_TYPE, URN_TYPE_VALUE);
    }

    @Override
    public String getResponderRole(){
        return domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_PARTYID_RESPONDER_ROLE, DEFAULT_RESPONDER_ROLE);
    }
}

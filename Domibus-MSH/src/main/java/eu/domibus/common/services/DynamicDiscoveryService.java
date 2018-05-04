package eu.domibus.common.services;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.util.EndpointInfo;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 3.2.5
 */
public interface DynamicDiscoveryService {
    String SMLZONE_KEY = "domibus.smlzone";
    String transportProfileAS4 = "bdxr-transport-ebms3-as4-v1p0";
    String DYNAMIC_DISCOVERY_MODE = "domibus.dynamic.discovery.peppolclient.mode";
    String DYNAMIC_DISCOVERY_CERT_REGEX = "domibus.dynamic.discovery.oasisclient.regexCertificateSubjectValidation";
    String DYNAMIC_DISCOVERY_PARTYID_ROLE = "domibus.dynamic.discovery.partyid.role";
    String DYNAMIC_DISCOVERY_PARTYID_TYPE = "domibus.dynamic.discovery.partyid.type";

    EndpointInfo lookupInformation(final String receiverId, final String receiverIdType, final String documentId, final String processId, final String processIdType) throws EbMS3Exception;

    String getPartyIdType();
    String getResponderRole();

}
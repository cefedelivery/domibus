package eu.domibus.common.services;

import eu.domibus.common.util.EndpointInfo;

/**
 * Created by idragusa on 3/29/17.
 */
public interface DynamicDiscoveryService {
    String SMLZONE_KEY = "domibus.smlzone";
    String transportProfileAS4 = "bdxr-transport-ebms3-as4-v1p0";

    EndpointInfo lookupInformation(final String receiverId, final String receiverIdType, final String documentId, final String processId, final String processIdType);

}
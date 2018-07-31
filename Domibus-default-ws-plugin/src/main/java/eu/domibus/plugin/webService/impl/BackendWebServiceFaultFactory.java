package eu.domibus.plugin.webService.impl;

import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.webService.generated.ErrorCode;
import eu.domibus.plugin.webService.generated.FaultDetail;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class BackendWebServiceFaultFactory {

    public FaultDetail generateFaultDetail(MessagingProcessingException mpEx) {
        FaultDetail fd = BackendWebServiceImpl.WEBSERVICE_OF.createFaultDetail();
        fd.setCode(mpEx.getEbms3ErrorCode().getErrorCodeName());
        fd.setMessage(mpEx.getMessage());
        return fd;
    }

    public FaultDetail generateDefaultFaultDetail(String message) {
        FaultDetail fd = BackendWebServiceImpl.WEBSERVICE_OF.createFaultDetail();
        fd.setCode(ErrorCode.EBMS_0004.name());
        fd.setMessage(message);
        return fd;
    }
}

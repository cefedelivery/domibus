package eu.domibus.ebms3.receiver;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class BindingHelper {
    //this is a hack to avoid a nullpointer in @see WebFaultOutInterceptor.
    //I suppose the bindingOperation is set after the execution of this interceptor and is empty in case of error.
    public void setBindingOperation(SoapMessage message) {
        final Exchange exchange = message.getExchange();
        if (exchange == null) {
            return;
        }
        final Endpoint endpoint = exchange.getEndpoint();
        if (endpoint == null) {
            return;
        }
        final EndpointInfo endpointInfo = endpoint.getEndpointInfo();
        if (endpointInfo == null) {
            return;
        }
        final BindingInfo binding = endpointInfo.getBinding();
        if (binding == null) {
            return;
        }
        final Collection<BindingOperationInfo> operations = binding.getOperations();
        if (operations == null) {
            return;
        }
        for (BindingOperationInfo operation : operations) {
            exchange.put(BindingOperationInfo.class, operation);
        }
    }

}

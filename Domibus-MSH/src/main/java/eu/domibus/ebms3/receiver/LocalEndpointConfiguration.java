package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.sender.MSHDispatcher;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Configuration
public class LocalEndpointConfiguration {

    @Autowired
    Bus bus;

    @Autowired
    MSHSourceMessageWebservice mshWebserviceSerializer;

    @Autowired
    SaveRequestToFileInInterceptor saveRequestToFileInInterceptor;

    @Bean(name = "localMSH")
    public Endpoint createMSHEndpoint() {
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);

        LocalTransportFactory localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", localTransport);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/http", localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/bindings/xformat", localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/transports/local", localTransport);


        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator("http://cxf.apache.org/transports/local", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/http", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", localTransport);
        extension.registerConduitInitiator("http://cxf.apache.org/bindings/xformat", localTransport);


        EndpointImpl endpoint = new EndpointImpl(bus, mshWebserviceSerializer);
        endpoint.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        endpoint.getInInterceptors().add(saveRequestToFileInInterceptor);
        endpoint.getInInterceptors().add(new SetPolicyInInterceptor.CheckEBMSHeaderInterceptor());
        endpoint.setAddress(MSHDispatcher.LOCAL_MSH_ENDPOINT);
        endpoint.publish();

        return endpoint;
    }
}

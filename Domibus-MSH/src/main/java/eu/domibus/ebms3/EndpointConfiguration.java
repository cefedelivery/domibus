package eu.domibus.ebms3;

import eu.domibus.ebms3.receiver.MSHWebserviceSerializerImpl;
import eu.domibus.ebms3.receiver.SetPolicyInInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Configuration
public class EndpointConfiguration {

    @Autowired
    Bus bus;

    @Autowired
    MSHWebserviceSerializerImpl MSHWebserviceSerializer;

    @Bean(name = "localMSH")
    public Endpoint createMSHEndpoint() throws IOException {
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);

        LocalTransportFactory localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", localTransport);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/http", localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/bindings/xformat", localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/transports/local", localTransport);


//
        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator("http://cxf.apache.org/transports/local", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/http", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", localTransport);
        extension.registerConduitInitiator("http://cxf.apache.org/bindings/xformat", localTransport);


        EndpointImpl endpoint = new EndpointImpl(bus, MSHWebserviceSerializer);
        endpoint.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        endpoint.getInInterceptors().add(new SaveRequestToFileInInterceptor());
        endpoint.getInInterceptors().add(new SetPolicyInInterceptor.CheckEBMSHeaderInterceptor());
        endpoint.setAddress("local://hello");
        endpoint.publish();

        /*EndpointInfo ei = new EndpointInfo(null, "http://cxf.apache.org/transports/local");
        ei.setAddress("local://hello");
        final Destination destination = localTransport.getDestination(ei, bus);
        destination.setMessageObserver(new MessageObserver() {
            @Override
            public void onMessage(Message message) {
                message.getExchange().put(ClientImpl.FINISHED, "true");
                System.out.println(message);
            }
        });*/

        return endpoint;
    }
}

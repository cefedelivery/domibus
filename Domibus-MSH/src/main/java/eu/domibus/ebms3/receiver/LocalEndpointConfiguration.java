package eu.domibus.ebms3.receiver;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.util.MessageUtil;
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
    protected Bus bus;

    @Autowired
    protected MSHSourceMessageWebservice mshWebserviceSerializer;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected MessageUtil messageUtil;

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
        endpoint.getInInterceptors().add(createSaveRequestToFileInInterceptor());
        endpoint.getInInterceptors().add(new SetPolicyInInterceptor.CheckEBMSHeaderInterceptor());
        endpoint.setAddress(MSHDispatcher.LOCAL_MSH_ENDPOINT);
        endpoint.publish();

        return endpoint;
    }

    protected SaveRequestToFileInInterceptor createSaveRequestToFileInInterceptor() {
        SaveRequestToFileInInterceptor result = new SaveRequestToFileInInterceptor();
        result.setDomainContextProvider(domainContextProvider);
        result.setDomibusPropertyProvider(domibusPropertyProvider);
        result.setMessageUtil(messageUtil);
        result.setSplitAndJoinService(splitAndJoinService);
        return result;
    }
}

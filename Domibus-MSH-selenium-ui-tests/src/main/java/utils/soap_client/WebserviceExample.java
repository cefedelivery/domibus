package utils.soap_client;


import com.sun.xml.internal.ws.developer.JAXWSProperties;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.BackendService11;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import utils.PROPERTIES;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class WebserviceExample {
    private static final Log LOG = LogFactory.getLog(WebserviceExample.class);

    private String wsdl;
    private static final String DEFAULT_WEBSERVICE_LOCATION = PROPERTIES.UI_BASE_URL + "services/backend?wsdl";


    public WebserviceExample()  {
        this(DEFAULT_WEBSERVICE_LOCATION);
    }

    public WebserviceExample(String webserviceLocation)  {
        this.wsdl = webserviceLocation;
    }

    public BackendInterface getPort() throws MalformedURLException {
        return getPort(null, null);
    }

    public BackendInterface getPort(String username, String password) throws MalformedURLException {
        if (wsdl == null || wsdl.isEmpty()) {
            throw new IllegalArgumentException("No webservice location specified");
        }

        BackendService11 backendService = new BackendService11(new URL(wsdl),  new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));
        BackendInterface backendPort = backendService.getBACKENDPORT();

        //enable chunking
        BindingProvider bindingProvider = (BindingProvider) backendPort;
        if(username != null && !username.isEmpty()) {
//            LOG.debug("Adding username [" + username + "] to the requestContext");
            bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
        }
        if(password != null && !password.isEmpty()) {
//            LOG.debug("Adding password to the requestContext");
            bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
        }

        Map<String, Object> ctxt = bindingProvider.getRequestContext();
        ctxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        //enable MTOM
        SOAPBinding binding = (SOAPBinding)bindingProvider.getBinding();
        binding.setMTOMEnabled(true);


        //comment the following lines if sending large files
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
//        handlers.add(new MessageLoggingHandler());
        bindingProvider.getBinding().setHandlerChain(handlers);

        return backendPort;
    }
}

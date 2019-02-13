package utils.soap_client;

//import eu.domibus.example.ws.logging.MessageLoggingHandler;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;


public class WebserviceExample {
//    private static final Log LOG = LogFactory.getLog(WebserviceExample.class);

//    private String wsdl;
//    private static final String DEFAULT_WEBSERVICE_LOCATION = "http://localhost:8080/domibus/services/backend?wsdl";
//
//
//    public WebserviceExample()  {
//        this(DEFAULT_WEBSERVICE_LOCATION);
//    }
//
//    public WebserviceExample(String webserviceLocation)  {
//        this.wsdl = webserviceLocation;
//    }
//
//    public BackendInterface getPort() throws MalformedURLException {
//        return getPort(null, null);
//    }
//
//    public BackendInterface getPort(String username, String password) throws MalformedURLException {
//        if (wsdl == null || wsdl.isEmpty()) {
//            throw new IllegalArgumentException("No webservice location specified");
//        }
//
//        BackendService11 backendService = new BackendService11(new URL(wsdl),  new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));
//        BackendInterface backendPort = backendService.getBACKENDPORT();
//
//        //enable chunking
//        BindingProvider bindingProvider = (BindingProvider) backendPort;
//        if(username != null && !username.isEmpty()) {
////            LOG.debug("Adding username [" + username + "] to the requestContext");
//            bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
//        }
//        if(password != null && !password.isEmpty()) {
////            LOG.debug("Adding password to the requestContext");
//            bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
//        }
//
//        Map<String, Object> ctxt = bindingProvider.getRequestContext();
//        ctxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        //enable MTOM
//        SOAPBinding binding = (SOAPBinding)bindingProvider.getBinding();
//        binding.setMTOMEnabled(true);
//
//
//        //comment the following lines if sending large files
//        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
////        handlers.add(new MessageLoggingHandler());
//        bindingProvider.getBinding().setHandlerChain(handlers);
//
//        return backendPort;
//    }
}

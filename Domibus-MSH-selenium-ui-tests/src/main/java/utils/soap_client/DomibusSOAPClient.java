package utils.soap_client;

import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.BackendService11;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DomibusSOAPClient {


//	private static final Log LOG = LogFactory.getLog(WebserviceExample.class);

	//	private static final String DEFAULT_WEBSERVICE_LOCATION = PROPERTIES.UI_BASE_URL + "/services/backend?wsdl";
	private static final String DEFAULT_WEBSERVICE_LOCATION = "http://localhost:8080/domibus/services/backend?wsdl";
	private String wsdl;


	public DomibusSOAPClient() {
		this(DEFAULT_WEBSERVICE_LOCATION);
	}

	public DomibusSOAPClient(String webserviceLocation) {
		this.wsdl = webserviceLocation;
	}

	public BackendInterface getPort() throws MalformedURLException {
		if (wsdl == null || wsdl.isEmpty()) {
			throw new IllegalArgumentException("No webservice location specified");
		}

		BackendService11 backendService = new BackendService11(new URL(wsdl), new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));

		BackendInterface backendPort = backendService.getBACKENDPORT();

		BindingProvider bindingProvider = (BindingProvider) backendPort;

		List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
		bindingProvider.getBinding().setHandlerChain(handlers);

		return backendPort;
	}

}


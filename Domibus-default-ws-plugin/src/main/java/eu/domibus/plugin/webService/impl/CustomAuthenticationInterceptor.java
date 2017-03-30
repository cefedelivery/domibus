package eu.domibus.plugin.webService.impl;

import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.services.AuthenticationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

@Component(value = "customAuthenticationInterceptor")
public class CustomAuthenticationInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomAuthenticationInterceptor.class);


    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Autowired
    private AuthenticationService authenticationService;

    public CustomAuthenticationInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        HttpServletRequest httpRequest = (HttpServletRequest) message.get("HTTP.REQUEST");

        LOG.debug("Intercepted request for " + httpRequest.getPathInfo());

        try {
            authenticationService.authenticate(httpRequest);
        } catch (AuthenticationException e) {
            throw new Fault(e);
        }
    }

}

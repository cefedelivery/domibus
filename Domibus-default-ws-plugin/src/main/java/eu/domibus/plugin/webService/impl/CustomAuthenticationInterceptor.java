package eu.domibus.plugin.webService.impl;

import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component(value = "customAuthenticationInterceptor")
public class CustomAuthenticationInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomAuthenticationInterceptor.class);

    @Autowired
    private AuthenticationExtService authenticationExtService;

    public CustomAuthenticationInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        HttpServletRequest httpRequest = (HttpServletRequest) message.get("HTTP.REQUEST");

        LOG.debug("Intercepted request for " + httpRequest.getPathInfo());

        try {
            authenticationExtService.authenticate(httpRequest);
        } catch (AuthenticationExtException e) {
            throw new Fault(e);
        }
    }

}

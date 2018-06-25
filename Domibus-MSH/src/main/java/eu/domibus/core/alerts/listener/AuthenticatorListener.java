package eu.domibus.core.alerts.listener;

import eu.domibus.core.alerts.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatorListener {

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticatorListener.class);

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'loginFailure'")
    public void onLoginFailure(final Event event) {

    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'accountDisabled'")
    public void onAccountDisabled(final Event event) {

    }

}

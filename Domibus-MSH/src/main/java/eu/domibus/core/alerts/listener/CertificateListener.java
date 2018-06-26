package eu.domibus.core.alerts.listener;

import eu.domibus.core.alerts.model.service.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class CertificateListener {

    private final static Logger LOG = LoggerFactory.getLogger(CertificateListener.class);

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'certificateImminentRevocation'")
    public void onImminentRevocationCertificateEvent(final Event event) {

    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'certificateRevoked'")
    public void onRevokedCertificateEvent(final Event event) {

    }

}

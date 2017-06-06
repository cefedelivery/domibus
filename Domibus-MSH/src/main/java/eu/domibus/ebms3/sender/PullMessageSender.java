package eu.domibus.ebms3.sender;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPMessage;

/**
 * Created by dussath on 6/2/17.
 *
 * Jms listener in charge of sending pullrequest.
 */
@Component
public class PullMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullMessageSender.class);
    @Autowired
    private MSHDispatcher mshDispatcher;
    @Autowired
    private EbMS3MessageBuilder messageBuilder;
    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;



    @PostConstruct
    public void init(){

    }
    @JmsListener(destination = "domibus.internal.pull.queue",containerFactory = "internalJmsListenerContainerFactory")
    public void processPullRequest(final MapMessage map) {
        try {
            final String mpc = map.getString(PullContext.MPC);
            final String pMode = map.getString(PullContext.PMODE_KEY);
            SignalMessage signalMessage = new SignalMessage();
            PullRequest pullRequest = new PullRequest();
            pullRequest.setMpc(mpc);
            signalMessage.setPullRequest(pullRequest);
            LOG.info("Sending message");
            SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(signalMessage, null);
            final SOAPMessage response = mshDispatcher.dispatch(soapMessage, pMode);
            Messaging messaging= MessageUtil.getMessage(response,jaxbContext);
        } catch (EbMS3Exception|JMSException  e) {
            LOG.error(e.getMessage(), e);
        }
    }
}

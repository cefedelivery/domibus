package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.*;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageImpl;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class UserMessageLegConfigurationFactoryTest {

    @Test
    public void testUserMessageConfigurationFactory() {
        Messaging messaging = new Messaging();
        messaging.setUserMessage(new UserMessage());
        SoapMessage soapMessage = new SoapMessage(new MessageImpl());
        UserMessageLegConfigurationFactory userMessageLegConfigurationFactory = new UserMessageLegConfigurationFactory();
        MessageLegConfigurationVisitor mock = Mockito.mock(MessageLegConfigurationVisitor.class);
        userMessageLegConfigurationFactory.setMessageLegConfigurationVisitor(mock);
        userMessageLegConfigurationFactory.chain(new PullRequestLegConfigurationFactory()).chain(new ReceiptLegConfigurationFactory());
        LegConfigurationExtractor legConfigurationExtractor = userMessageLegConfigurationFactory.extractMessageConfiguration(soapMessage, messaging);
        assertTrue(legConfigurationExtractor instanceof UserMessageLegConfigurationExtractor);
        Mockito.verify(mock,Mockito.times(1)).visit(Mockito.any(UserMessageLegConfigurationExtractor.class));
        Mockito.verify(mock,Mockito.times(0)).visit(Mockito.any(PullRequestLegConfigurationExtractor.class));
        Mockito.verify(mock,Mockito.times(0)).visit(Mockito.any(ReceiptLegConfigurationExtractor.class));
    }

    @Test
    public void testPullRequestConfigurationFactory() {
        Messaging messaging = new Messaging();
        SoapMessage soapMessage = new SoapMessage(new MessageImpl());
        SignalMessage signalMessage = new SignalMessage();
        signalMessage.setPullRequest(new PullRequest());
        messaging.setSignalMessage(signalMessage);
        UserMessageLegConfigurationFactory userMessageLegConfigurationFactory = new UserMessageLegConfigurationFactory();
        MessageLegConfigurationVisitor mock = Mockito.mock(MessageLegConfigurationVisitor.class);
        PullRequestLegConfigurationFactory pullRequestLegConfigurationFactory = new PullRequestLegConfigurationFactory();
        pullRequestLegConfigurationFactory.setMessageLegConfigurationVisitor(mock);
        userMessageLegConfigurationFactory.chain(pullRequestLegConfigurationFactory).chain(new ReceiptLegConfigurationFactory());
        LegConfigurationExtractor legConfigurationExtractor = userMessageLegConfigurationFactory.extractMessageConfiguration(soapMessage, messaging);
        assertTrue(legConfigurationExtractor instanceof PullRequestLegConfigurationExtractor);
        Mockito.verify(mock,Mockito.times(0)).visit(Mockito.any(UserMessageLegConfigurationExtractor.class));
        Mockito.verify(mock,Mockito.times(1)).visit(Mockito.any(PullRequestLegConfigurationExtractor.class));
        Mockito.verify(mock,Mockito.times(0)).visit(Mockito.any(ReceiptLegConfigurationExtractor.class));
    }

    @Test
    public void testReceiptConfigurationFactory() {
        Messaging messaging = new Messaging();
        SoapMessage soapMessage = new SoapMessage(new MessageImpl());
        SignalMessage signalMessage = new SignalMessage();
        signalMessage.setReceipt(new Receipt());
        messaging.setSignalMessage(signalMessage);
        UserMessageLegConfigurationFactory userMessageLegConfigurationFactory = new UserMessageLegConfigurationFactory();
        MessageLegConfigurationVisitor mock = Mockito.mock(MessageLegConfigurationVisitor.class);
        PullRequestLegConfigurationFactory pullRequestLegConfigurationFactory = new PullRequestLegConfigurationFactory();
        ReceiptLegConfigurationFactory receiptLegConfigurationFactory = new ReceiptLegConfigurationFactory();
        receiptLegConfigurationFactory.setMessageLegConfigurationVisitor(mock);
        userMessageLegConfigurationFactory.chain(pullRequestLegConfigurationFactory).chain(receiptLegConfigurationFactory);
        LegConfigurationExtractor legConfigurationExtractor = userMessageLegConfigurationFactory.extractMessageConfiguration(soapMessage, messaging);
        assertTrue(legConfigurationExtractor instanceof ReceiptLegConfigurationExtractor);
        Mockito.verify(mock,Mockito.times(0)).visit(Mockito.any(UserMessageLegConfigurationExtractor.class));
        Mockito.verify(mock,Mockito.times(0)).visit(Mockito.any(PullRequestLegConfigurationExtractor.class));
        Mockito.verify(mock,Mockito.times(1)).visit(Mockito.any(ReceiptLegConfigurationExtractor.class));
    }

}
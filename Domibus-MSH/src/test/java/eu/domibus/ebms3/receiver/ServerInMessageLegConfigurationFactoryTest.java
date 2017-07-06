package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@RunWith(MockitoJUnitRunner.class)
public class ServerInMessageLegConfigurationFactoryTest {

    @Mock
    private UserMessageLegConfigurationFactory userMessageLegConfigurationFactory;
    @Mock
    private PullRequestLegConfigurationFactory pullRequestLegConfigurationFactory;
    @Mock
    private ReceiptLegConfigurationFactory receiptLegConfigurationFactory;
    @InjectMocks
    private ServerInMessageLegConfigurationFactory configurationFactory;

    @Test
    public void extractUserMessageConfiguration() throws Exception {
        Messaging messaging = new Messaging();
        messaging.setUserMessage(new UserMessage());
        SoapMessage soapMessage = new SoapMessage(new MessageImpl());
        configurationFactory.extractMessageConfiguration(soapMessage, messaging);
        verify(userMessageLegConfigurationFactory, times(1)).
                extractMessageConfiguration(Mockito.eq(soapMessage), Mockito.eq(messaging));
    }



}
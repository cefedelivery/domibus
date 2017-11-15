package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.usermessage.domain.*;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.delegate.services.security.SecurityService;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.UserMessageException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JMockit.class)
public class UserMessageServiceDelegateTest {

    @Tested
    UserMessageServiceDelegate userMessageServiceDelegate;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    DomainExtConverter domainConverter;

    @Injectable
    SecurityService securityService;

    @Test
    public void testGetMessageSuccess() {
        // Given
        final String messageId = "messageId";

        final UserMessage userMessage = new UserMessage();
        final MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(messageId);
        messageInfo.setRefToMessageId("refToMessageId");
        messageInfo.setTimestamp(new Date());
        userMessage.setMessageInfo(messageInfo);
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("action");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setPmode("pmode");
        agreementRef.setType("type");
        agreementRef.setValue("value");
        collaborationInfo.setAgreementRef(agreementRef);
        collaborationInfo.setConversationId("conversationId");
        Service service = new Service();
        service.setType("type");
        service.setValue("value");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);

        new Expectations() {{
            userMessageService.getMessage(messageId);
            result = userMessage;
        }};


        // When
        userMessageServiceDelegate.getMessage(messageId);

        // Then
        new Verifications() {{
            userMessageService.getMessage(messageId);
            domainConverter.convert(userMessage, UserMessageDTO.class);
        }};
    }

    @Test
    public void testGetMessageException() {
        // Given
        final UserMessageException userMessageException = new UserMessageException(DomibusErrorCode.DOM_001, "test");
        final String messageId = "messageId";

        new Expectations() {{
            userMessageService.getMessage(messageId);
            result = userMessageException;
        }};

        // When
        try {
            userMessageServiceDelegate.getMessage(messageId);
        } catch (UserMessageException e) {
            // Then
            Assert.assertTrue(userMessageException == e);
            return;
        }
        Assert.fail();
    }
}

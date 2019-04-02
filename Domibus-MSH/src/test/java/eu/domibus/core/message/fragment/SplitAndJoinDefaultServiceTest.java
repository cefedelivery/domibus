package eu.domibus.core.message.fragment;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Splitting;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.AS4ReceiptService;
import eu.domibus.common.services.impl.MessageRetentionService;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.AttachmentCleanupService;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.handler.IncomingSourceMessageHandler;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import eu.domibus.pki.PolicyService;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class SplitAndJoinDefaultServiceTest {

    @Tested
    SplitAndJoinDefaultService splitAndJoinDefaultService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected MessagingDao messagingDao;

    @Injectable
    protected MessageGroupDao messageGroupDao;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected SoapUtil soapUtil;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected StorageProvider storageProvider;

    @Injectable
    protected MessageUtil messageUtil;

    @Injectable
    protected UserMessageDefaultService userMessageDefaultService;

    @Injectable
    protected UserMessageLogDao userMessageLogDao;

    @Injectable
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    protected AttachmentCleanupService attachmentCleanupService;

    @Injectable
    protected UserMessageHandlerService userMessageHandlerService;

    @Injectable
    protected MessagingService messagingService;

    @Injectable
    protected UserMessageService userMessageService;

    @Injectable
    protected IncomingSourceMessageHandler incomingSourceMessageHandler;

    @Injectable
    protected PolicyService policyService;

    @Injectable
    protected MSHDispatcher mshDispatcher;

    @Injectable
    protected AS4ReceiptService as4ReceiptService;

    @Injectable
    protected EbMS3MessageBuilder messageBuilder;

    @Injectable
    protected MessageRetentionService messageRetentionService;

    @Injectable
    protected MessageGroupService messageGroupService;

    @Test
    public void createUserFragmentsFromSourceFile(@Injectable SOAPMessage sourceMessageRequest,
                                                  @Injectable UserMessage userMessage,
                                                  @Injectable MessageExchangeConfiguration userMessageExchangeConfiguration,
                                                  @Injectable LegConfiguration legConfiguration,
                                                  @Mocked File file) throws EbMS3Exception, IOException {
        String sourceMessageFileName = "invoice.pdf";
        long sourceMessageFileLength = 23L;
        String contentTypeString = "application/pdf";
        boolean compression = false;
        String pModeKey = "mykey";
        String sourceMessageId = "123";
        String groupId = sourceMessageId;


        List<String> fragmentFiles = new ArrayList<>();
        fragmentFiles.add("fragment1");
        fragmentFiles.add("fragment2");

        new Expectations(splitAndJoinDefaultService) {{
            userMessage.getMessageInfo().getMessageId();
            result = groupId;

            userMessage.getMessageInfo().getMessageId();
            result = sourceMessageId;

            new File(sourceMessageFileName);
            result = file;

            file.delete();
            result = true;

            file.length();
            result = sourceMessageFileLength;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = userMessageExchangeConfiguration;

            userMessageExchangeConfiguration.getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;


            splitAndJoinDefaultService.splitSourceMessage((File) any, anyInt);
            result = fragmentFiles;
        }};

        splitAndJoinDefaultService.createUserFragmentsFromSourceFile(sourceMessageFileName, sourceMessageRequest, userMessage, contentTypeString, compression);

        new Verifications() {{
            MessageGroupEntity messageGroupEntity = null;
            userMessageDefaultService.createMessageFragments(userMessage, messageGroupEntity = withCapture(), fragmentFiles);

            Assert.assertEquals(messageGroupEntity.getFragmentCount().longValue(), 2L);
            Assert.assertEquals(messageGroupEntity.getSourceMessageId(), sourceMessageId);
            Assert.assertEquals(messageGroupEntity.getGroupId(), groupId);
            Assert.assertEquals(messageGroupEntity.getMessageSize(), BigInteger.valueOf(sourceMessageFileLength));

            attachmentCleanupService.cleanAttachments(sourceMessageRequest);
        }};
    }

    @Test
    public void rejoinSourceMessage(@Injectable final SOAPMessage sourceRequest,
                                    @Injectable final Messaging messaging,
                                    @Injectable MessageExchangeConfiguration userMessageExchangeConfiguration,
                                    @Injectable LegConfiguration legConfiguration
    ) throws EbMS3Exception, TransformerException, SOAPException {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;
        String sourceMessageFile = "invoice.pdf";
        String backendName = "mybackend";
        String pModeKey = "mykey";
        String reversePModeKey = "reversemykey";

        new Expectations(splitAndJoinDefaultService) {{
            splitAndJoinDefaultService.rejoinSourceMessage(groupId, (File) any);
            result = sourceRequest;

            messageUtil.getMessage(sourceRequest);
            result = messaging;

            pModeProvider.findUserMessageExchangeContext(messaging.getUserMessage(), MSHRole.RECEIVING);
            result = userMessageExchangeConfiguration;

            userMessageExchangeConfiguration.getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = sourceMessageId;

            userMessageExchangeConfiguration.getReversePmodeKey();
            result = reversePModeKey;
        }};

        splitAndJoinDefaultService.rejoinSourceMessage(groupId, sourceMessageFile, backendName);

        new Verifications() {{
            userMessageHandlerService.handlePayloads(sourceRequest, messaging.getUserMessage());
            messagingService.storePayloads(messaging, MSHRole.RECEIVING, legConfiguration, backendName);
            messageGroupService.setSourceMessageId(sourceMessageId, groupId);
            incomingSourceMessageHandler.processMessage(sourceRequest, messaging);
            userMessageService.scheduleSourceMessageReceipt(sourceMessageId, reversePModeKey);
        }};
    }

    @Test
    public void sendSourceMessageReceipt(@Injectable final SOAPMessage sourceRequest) throws EbMS3Exception {
        String sourceMessageId = "123";
        String pModeKey = "mykey";

        new Expectations(splitAndJoinDefaultService) {{
            as4ReceiptService.generateReceipt(sourceMessageId, false);
            result = sourceRequest;

            splitAndJoinDefaultService.sendSignalMessage(sourceRequest, pModeKey);
        }};

        splitAndJoinDefaultService.sendSourceMessageReceipt(sourceMessageId, pModeKey);

        new Verifications() {{
            splitAndJoinDefaultService.sendSignalMessage(sourceRequest, pModeKey);
            times = 1;
        }};
    }

    @Test
    public void sendSignalError(@Injectable SOAPMessage soapMessage) throws EbMS3Exception {
        String messageId = "123";
        String pModeKey = "mykey";
        String ebMS3ErrorCode = ErrorCode.EbMS3ErrorCode.EBMS_0004.getCode().getErrorCode().getErrorCodeName();
        String errorDetail = "Split and Joing error";

        new Expectations() {{
            messageBuilder.buildSOAPFaultMessage((Error) any);
            result = soapMessage;
        }};

        splitAndJoinDefaultService.sendSignalError(messageId, ebMS3ErrorCode, errorDetail, pModeKey);

        new Verifications() {{
            Error error = null;
            messageBuilder.buildSOAPFaultMessage(error = withCapture());

            Assert.assertEquals(error.getErrorCode(), ebMS3ErrorCode);
            Assert.assertEquals(error.getErrorDetail(), errorDetail);

            splitAndJoinDefaultService.sendSignalMessage(soapMessage, pModeKey);
        }};
    }

    @Test
    public void sendSignalMessage(@Injectable SOAPMessage soapMessage,
                                  @Injectable LegConfiguration legConfiguration,
                                  @Injectable Party receiverParty,
                                  @Injectable Policy policy
    ) throws EbMS3Exception {
        String pModeKey = "mykey";
        String endpoint = "http://localhost/msh";

        new Expectations() {{
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getEndpoint();
            result = endpoint;

            policyService.getPolicy(legConfiguration);
            result = policy;
        }};

        splitAndJoinDefaultService.sendSignalMessage(soapMessage, pModeKey);

        new Verifications() {{
            mshDispatcher.dispatch(soapMessage, endpoint, policy, legConfiguration, pModeKey);
        }};
    }

    @Test
    public void mayUseSplitAndJoin(@Injectable LegConfiguration legConfiguration,
                                   @Injectable Splitting splitting) {
        new Expectations() {{
            legConfiguration.getSplitting();
            result = splitting;
        }};

        Assert.assertTrue(splitAndJoinDefaultService.mayUseSplitAndJoin(legConfiguration));
    }

    @Test
    public void generateSourceFileName(@Mocked UUID uuid) {
        String directory = "/home/temp";
        String uuidValue = "123";

        new Expectations() {{
            UUID.randomUUID().toString();
            result = uuidValue;
        }};

        final String generateSourceFileName = splitAndJoinDefaultService.generateSourceFileName(directory);

        Assert.assertEquals(generateSourceFileName, directory + "/" + uuidValue);
        ;
    }

    @Test
    public void rejoinMessageFragments(@Injectable MessageGroupEntity messageGroupEntity,
                                       @Mocked UserMessage userMessage1,
                                       @Injectable PartInfo partInfo

    ) {
        String sourceMessageId = "123";
        String groupId = sourceMessageId;
        String fileName = "invoice.pdf";

        List<UserMessage> userMessageFragments = new ArrayList<>();
        userMessageFragments.add(userMessage1);

        List<PartInfo> partInfoList = new ArrayList<>();
        partInfoList.add(partInfo);

        new Expectations(splitAndJoinDefaultService) {{
            messageGroupDao.findByGroupId(groupId);
            result = messageGroupEntity;

            messagingDao.findUserMessageByGroupId(groupId);
            result = userMessageFragments;

            messageGroupEntity.getFragmentCount();
            result = 1;

            userMessage1.getPayloadInfo().getPartInfo();
            result = partInfoList;

            partInfo.getFileName();
            result = fileName;

            splitAndJoinDefaultService.mergeSourceFile((List<File>) any, messageGroupEntity);
        }};

        splitAndJoinDefaultService.rejoinMessageFragments(groupId);

        new Verifications() {{
            List<File> fragmentFilesInOrder = null;

            splitAndJoinDefaultService.mergeSourceFile(fragmentFilesInOrder = withCapture(), messageGroupEntity);

            Assert.assertEquals(fragmentFilesInOrder.size(), 1);
        }};
    }
}
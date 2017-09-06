package eu.domibus.ebms3.receiver;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationType;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.routing.CriteriaFactory;
import eu.domibus.plugin.routing.IRoutingCriteria;
import eu.domibus.plugin.routing.RoutingService;
import eu.domibus.plugin.routing.dao.BackendFilterDao;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import eu.domibus.submission.SubmissionValidatorListProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by baciuco on 08/08/2016.
 */
@RunWith(JMockit.class)
public class BackendNotificationServiceTest {

    @Injectable
    JMSManager jmsManager;

    @Injectable
    BackendFilterDao backendFilterDao;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    RoutingService routingService;

    @Injectable
    UserMessageLogDao messageLogDao;

    @Injectable
    SubmissionAS4Transformer submissionAS4Transformer;

    @Injectable
    SubmissionValidatorListProvider submissionValidatorListProvider;

    List<NotificationListener> notificationListenerServices;

    @Injectable
    List<CriteriaFactory> routingCriteriaFactories;

    @Injectable
    Queue unknownReceiverQueue;

    @Injectable
    ApplicationContext applicationContext;

    @Injectable
    Map<String, IRoutingCriteria> criteriaMap;

    @Injectable
    DomainCoreConverter coreConverter;

    @Injectable
    MessageExchangeService messageExchangeService;

    @Tested
    BackendNotificationService backendNotificationService = new BackendNotificationService();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Injectable
    Properties domibusProperties;

    @Test
    public void testValidateSubmissionForUnsupportedNotificationType(@Injectable final Submission submission, @Injectable final UserMessage userMessage) throws Exception {
        final String backendName = "customPlugin";
        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED_FAILURE);

        new Verifications() {{
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            times = 0;
        }};
    }

    @Test
    public void testValidateSubmissionWhenFirstValidatorThrowsException(@Injectable final Submission submission,
                                                                        @Injectable final UserMessage userMessage,
                                                                        @Injectable final SubmissionValidatorList submissionValidatorList,
                                                                        @Injectable final SubmissionValidator validator1,
                                                                        @Injectable final SubmissionValidator validator2) throws Exception {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionAS4Transformer.transformFromMessaging(userMessage);
            result = submission;
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = submissionValidatorList;
            submissionValidatorList.getSubmissionValidators();
            result = Arrays.asList(new SubmissionValidator[]{validator1, validator2});
            validator1.validate(submission);
            result = new SubmissionValidationException("Exception in the validator1");
        }};

        thrown.expect(SubmissionValidationException.class);
        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new Verifications() {{
            validator2.validate(submission);
            times = 0;
        }};
    }

    @Test
    public void testValidateSubmissionWithAllValidatorsCalled(@Injectable final Submission submission,
                                                              @Injectable final UserMessage userMessage,
                                                              @Injectable final SubmissionValidatorList submissionValidatorList,
                                                              @Injectable final SubmissionValidator validator1,
                                                              @Injectable final SubmissionValidator validator2) throws Exception {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionAS4Transformer.transformFromMessaging(userMessage);
            result = submission;
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = submissionValidatorList;
            submissionValidatorList.getSubmissionValidators();
            result = Arrays.asList(new SubmissionValidator[]{validator1, validator2});
        }};

        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new Verifications() {{
            validator1.validate(submission);
            times = 1;
            validator2.validate(submission);
            times = 1;
        }};
    }

    @Test
    public void testGetNotificationListener(@Injectable final NotificationListener notificationListener1,
                                            @Injectable final NotificationListener notificationListener2) throws Exception {
        final String backendName = "customPlugin";
        new Expectations() {{
            notificationListener1.getBackendName();
            result = "anotherPlugin";
            notificationListener2.getBackendName();
            result = backendName;
        }};

        List<NotificationListener> notificationListeners = new ArrayList<>();
        notificationListeners.add(notificationListener1);
        notificationListeners.add(notificationListener2);
        backendNotificationService.notificationListenerServices = notificationListeners;

        NotificationListener notificationListener = backendNotificationService.getNotificationListener(backendName);
        Assert.assertEquals(backendName, notificationListener.getBackendName());

    }

    @Test
    public void testValidateAndNotify(@Injectable final UserMessage userMessage,
                                      @Injectable final String backendName,
                                      @Injectable final NotificationType notificationType) throws Exception {
        new Expectations(backendNotificationService) {{
            backendNotificationService.validateSubmission(userMessage, backendName, notificationType);
            result = null;
            backendNotificationService.notify(anyString, backendName, notificationType, null);
            result = null;
        }};

        Map<String, Object> properties = new HashMap<>();
        backendNotificationService.validateAndNotify(userMessage, backendName, notificationType, properties);

        new Verifications() {{
            backendNotificationService.validateSubmission(userMessage, backendName, notificationType);
            times = 1;
            backendNotificationService.notify(anyString, backendName, notificationType, null);
            times = 1;
        }};
    }

    @Test
    public void testNotifyWithNoConfiguredNoficationListener(
            @Injectable final NotificationType notificationType,
            @Injectable final Queue queue) throws Exception {
        final String backendName = "customPlugin";
        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(backendName);
            result = null;
        }};

        backendNotificationService.notify("messageId", backendName, notificationType);

        new Verifications() {{
            jmsManager.sendMessageToQueue(withAny(new JmsMessage()), withAny(queue));
            times = 0;
        }};
    }

    @Test
    public void testNotifyWithConfiguredNotificationListener(
            @Injectable final NotificationListener notificationListener,
            @Injectable final Queue queue) throws Exception {

        final String backendName = "customPlugin";

        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(backendName);
            result = notificationListener;

            notificationListener.getBackendNotificationQueue();
            result = queue;
        }};

        final String messageId = "123";
        final NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        backendNotificationService.notify(messageId, backendName, notificationType);

        new Verifications() {{
            JmsMessage jmsMessage = null;
            jmsManager.sendMessageToQueue(jmsMessage = withCapture(), queue);
            times = 1;

            Assert.assertEquals(jmsMessage.getProperty(MessageConstants.MESSAGE_ID), messageId);
            Assert.assertEquals(jmsMessage.getProperty(MessageConstants.NOTIFICATION_TYPE), notificationType.name());
        }};
    }

    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromAndActionMatching(@Injectable final BackendFilter filter,
                                                                                 @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                 @Injectable final UserMessage userMessage,
                                                                                 @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                                 @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                                 @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                 @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            // TODO: Criteria Operator is not used.
            /*filter.getCriteriaOperator();
            result = LogicalOperator.AND;*/

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            actionRoutingCriteria.getName();
            result = actionCriteriaName;

            actionRoutingCriteria.getExpression();
            result = "myAction";

            criteriaMap.get(actionCriteriaName);
            result = actionRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;

            actionRoutingCriteriaConfiguration.matches(userMessage, actionRoutingCriteria.getExpression());
            result = true;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertTrue(backendFilterMatching);
    }


    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromMatchingAndActionNotMatching(@Injectable final BackendFilter filter,
                                                                                            @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                            @Injectable final UserMessage userMessage,
                                                                                            @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                                            @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                                            @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                            @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.AND;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            actionRoutingCriteria.getName();
            result = actionCriteriaName;

            actionRoutingCriteria.getExpression();
            result = "myAction";

            criteriaMap.get(actionCriteriaName);
            result = actionRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;

            actionRoutingCriteriaConfiguration.matches(userMessage, actionRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);
    }


    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromNotMatching(@Injectable final BackendFilter filter,
                                                                           @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                           @Injectable final UserMessage userMessage,
                                                                           @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                           @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                           @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                           @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.AND;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);

        new Verifications() {{
            criteriaMap.get(actionCriteriaName);
            times = 0;

            actionRoutingCriteriaConfiguration.matches(userMessage, anyString);
            times = 0;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingWithFromMatchingAndActionNotMatching(@Injectable final BackendFilter filter,
                                                                                @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                @Injectable final UserMessage userMessage,
                                                                                @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                                @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                                @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.OR;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);

        new Verifications() {{
            criteriaMap.get(actionCriteriaName);
            times = 0;

            actionRoutingCriteriaConfiguration.matches(userMessage, anyString);
            times = 1;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingWithNoRoutingCriteriaDefined(@Injectable final BackendFilter filter,
                                                                        @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                        @Injectable final UserMessage userMessage,
                                                                        @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                        @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                        @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                        @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        new Expectations() {{
            filter.getRoutingCriterias();
            result = null;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertTrue(backendFilterMatching);

        new Verifications() {{
            criteriaMap.get(anyString);
            times = 0;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromNotMatchingAndActionMatching(@Injectable final BackendFilter filter,
                                                                                            @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                            @Injectable final UserMessage userMessage,
                                                                                            @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                                            @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration, //configured in the domibus-plugins.xml
                                                                                            @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                            @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.OR;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);
    }

    @Test
    public void testGetMatchingBackendFilter(@Injectable final UserMessage userMessage, @Injectable final List<BackendFilter> backendFilters) throws Exception {
        new Expectations(backendNotificationService) {{
            backendNotificationService.getBackendFilters();
            result = backendFilters;
        }};

        backendNotificationService.getMatchingBackendFilter(userMessage);

        new Verifications() {{
            backendNotificationService.getMatchingBackendFilter(backendFilters, withAny(new HashMap<String, IRoutingCriteria>()), userMessage);
        }};
    }

    @Test
    public void testNotifyOfMessageStatusChange(@Injectable final MessageLog messageLog) throws Exception {
        final String messageId = "1";
        final String backend = "JMS";

        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;

            messageLog.getMessageStatus();
            result = null;

            messageLog.getMessageId();
            result = messageId;

            messageLog.getBackend();
            result = backend;

            backendNotificationService.notify(anyString, anyString, NotificationType.MESSAGE_STATUS_CHANGE, withAny(new HashMap<String, Object>()));
        }};

        MessageStatus status = MessageStatus.ACKNOWLEDGED;
        backendNotificationService.notifyOfMessageStatusChange(messageLog, status, new Timestamp(System.currentTimeMillis()));

        new Verifications() {{
            String capturedMessageId = null;
            String capturedBackend = null;
            Map<String, Object> properties = null;
            backendNotificationService.notify(capturedMessageId = withCapture(), capturedBackend = withCapture(), NotificationType.MESSAGE_STATUS_CHANGE, properties = withCapture());

            Assert.assertEquals(messageId, capturedMessageId);
            Assert.assertEquals(capturedBackend, backend);
        }};
    }
}

package eu.domibus.ebms3.receiver;

import eu.domibus.common.NotificationType;
import eu.domibus.common.dao.MessageLogDao;
import eu.domibus.ebms3.common.model.UserMessage;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsOperations;

import javax.jms.Queue;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by baciuco on 08/08/2016.
 */
@RunWith(JMockit.class)
public class BackendNotificationServiceTest {

    @Injectable
    JmsOperations jmsTemplateNotify;

    @Injectable
    BackendFilterDao backendFilterDao;

    @Injectable
    RoutingService routingService;

    @Injectable
    MessageLogDao messageLogDao;

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

    @Tested
    BackendNotificationService backendNotificationService = new BackendNotificationService();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
}

package eu.domibus.ebms3.receiver;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.NotificationType;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.NotifyMessageCreator;
import eu.domibus.messaging.ReceiveFailedMessageCreator;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.routing.*;
import eu.domibus.plugin.routing.dao.BackendFilterDao;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import eu.domibus.plugin.validation.SubmissionValidator;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import eu.domibus.submission.SubmissionValidatorListProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service("backendNotificationService")
public class BackendNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(BackendNotificationService.class);

    @Autowired
    JMSManager jmsManager;

    @Autowired
    private BackendFilterDao backendFilterDao;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    protected SubmissionAS4Transformer submissionAS4Transformer;

    @Autowired
    protected SubmissionValidatorListProvider submissionValidatorListProvider;


    protected List<NotificationListener> notificationListenerServices;

    @Resource(name = "routingCriteriaFactories")
    protected List<CriteriaFactory> routingCriteriaFactories;

    @Autowired
    @Qualifier("unknownReceiverQueue")
    private Queue unknownReceiverQueue;

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, IRoutingCriteria> criteriaMap;


    @PostConstruct
    public void init() {
        Map notificationListenerBeanMap = applicationContext.getBeansOfType(NotificationListener.class);
        if (notificationListenerBeanMap.isEmpty()) {
            throw new ConfigurationException("No Plugin available! Please configure at least one backend plugin in order to run domibus");
        } else {
            notificationListenerServices = new ArrayList<NotificationListener>(notificationListenerBeanMap.values());
        }

        criteriaMap = new HashMap<>();
        for (final CriteriaFactory routingCriteriaFactory : routingCriteriaFactories) {
            criteriaMap.put(routingCriteriaFactory.getName(), routingCriteriaFactory.getInstance());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyOfIncomingFailure(final UserMessage userMessage) {
        notifyOfIncoming(userMessage, NotificationType.MESSAGE_RECEIVED_FAILURE);
    }

    public void notifyOfIncoming(final UserMessage userMessage, NotificationType notificationType) {
        LOG.debug("Notify of incoming message, notificationType is " + (notificationType == null ? "null" : notificationType.name()));
        List<BackendFilter> backendFilter = backendFilterDao.findAll();
        if (backendFilter.isEmpty()) { // There is no saved backendfilter configuration. Most likely the backends have not been configured yet
            backendFilter = routingService.getBackendFilters();
            if (backendFilter.isEmpty()) {
                LOG.error("There are no backend plugins deployed on this server");
            }
            if (backendFilter.size() > 1) { //There is more than one unconfigured backend available. For security reasons we cannot send the message just to the first one
                LOG.error("There are multiple un-configured backend plugins available. Please set up the configuration using the \"Message filter\" panel of the administrative GUI.");
                backendFilter.clear(); // empty the list so its handled in the desired way.
            }
            //If there is only one backend deployed we send it to that as this is most likely the intent
        }
        for (final BackendFilter filter : backendFilter) {
            boolean matches = true;
            for (final RoutingCriteria routingCriteria : filter.getRoutingCriterias()) {
                final IRoutingCriteria criteria = criteriaMap.get(routingCriteria.getName());
                matches = criteria.matches(userMessage, routingCriteria.getExpression());
                if (!matches) {
                    break;
                }
            }
            if (matches) {
                LOG.info("Notify backend " + filter.getBackendName() + " of message [" + userMessage.getMessageInfo().getMessageId() + "]");
                validateAndNotify(userMessage, filter.getBackendName(), notificationType);
                return;
            }
        }
        String finalRecipient = getFinalRecipient(userMessage);
        LOG.error("No backend responsible for message [" + userMessage.getMessageInfo().getMessageId() + "] found. Sending notification to [" + unknownReceiverQueue + "]");
        jmsManager.sendMessageToQueue(new NotifyMessageCreator(userMessage.getMessageInfo().getMessageId(), NotificationType.MESSAGE_RECEIVED, finalRecipient).createMessage(), unknownReceiverQueue);
    }

    private String getFinalRecipient(UserMessage userMessage) {
        String finalRecipient = null;
        for (final Property p : userMessage.getMessageProperties().getProperty()) {
            if (p.getName() != null && p.getName().equals(MessageConstants.FINAL_RECIPIENT)) {
                finalRecipient = p.getValue();
                break;
            }
        }
        return finalRecipient;
    }

    protected void validateSubmission(UserMessage userMessage, String backendName, NotificationType notificationType) {
        if (NotificationType.MESSAGE_RECEIVED != notificationType) {
            LOG.debug("Validation is not configured to be done for notification of type [" + notificationType + "]");
            return;
        }

        SubmissionValidatorList submissionValidatorList = submissionValidatorListProvider.getSubmissionValidatorList(backendName);
        if (submissionValidatorList == null) {
            LOG.debug("No submission validators found for backend [" + backendName + "]");
            return;
        }
        LOG.info("Performing submission validation for backend [" + backendName + "]");
        Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage);
        List<SubmissionValidator> submissionValidators = submissionValidatorList.getSubmissionValidators();
        for (SubmissionValidator submissionValidator : submissionValidators) {
            submissionValidator.validate(submission);
        }
    }

    protected NotificationListener getNotificationListener(String backendName) {
        for (final NotificationListener notificationListenerService : notificationListenerServices) {
            if (notificationListenerService.getBackendName().equals(backendName)) {
                return notificationListenerService;
            }
        }
        return null;
    }

    protected void validateAndNotify(UserMessage userMessage, String backendName, NotificationType notificationType) {
        validateSubmission(userMessage, backendName, notificationType);
        String finalRecipient = getFinalRecipient(userMessage);
        notify(userMessage.getMessageInfo().getMessageId(), backendName, notificationType, finalRecipient);
    }

        protected void notify(String messageId, String backendName, NotificationType notificationType) {
            notify(messageId, backendName, notificationType, null);
        }

        protected void notify(String messageId, String backendName, NotificationType notificationType, String finalRecipient) {
        NotificationListener notificationListener = getNotificationListener(backendName);
        if (notificationListener == null) {
            LOG.debug("No notification listeners found for backend [" + backendName + "]");
            return;
        }
        jmsManager.sendMessageToQueue(new NotifyMessageCreator(messageId, notificationType, finalRecipient).createMessage(), notificationListener.getBackendNotificationQueue());
//        jmsOperations.send(notificationListener.getBackendNotificationQueue(), new NotifyMessageCreator(messageId, notificationType));
    }

    public void notifyOfSendFailure(final String messageId) {
        final String backendName = userMessageLogDao.findBackendForMessageId(messageId);
        notify(messageId, backendName, NotificationType.MESSAGE_SEND_FAILURE);

    }

    public void notifyOfSendSuccess(final String messageId) {
        final String backendName = userMessageLogDao.findBackendForMessageId(messageId);
        notify(messageId, backendName, NotificationType.MESSAGE_SEND_SUCCESS);
    }

    public void notifyOfReceiveFailure(final String messageId, String endpoint) {
        final String backendName = userMessageLogDao.findBackendForMessageId(messageId);
        for (final NotificationListener notificationListenerService : notificationListenerServices) {
            if (notificationListenerService.getBackendName().equals(backendName)) {
                jmsManager.sendMessageToQueue(new ReceiveFailedMessageCreator(messageId, endpoint).createMessage(), notificationListenerService.getBackendNotificationQueue());

            }
        }
    }

    public List<NotificationListener> getNotificationListenerServices() {
        return notificationListenerServices;
    }
}

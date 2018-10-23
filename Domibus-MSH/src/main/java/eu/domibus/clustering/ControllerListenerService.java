package eu.domibus.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by kochc01 on 02.03.2016.
 */

@Service(value = "controllerListenerService")
public class ControllerListenerService implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ControllerListenerService.class);

    @Autowired
    protected CommandService commandService;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    @Transactional
    public void onMessage(Message message) {
        String command = null;
        try {
            command = message.getStringProperty(Command.COMMAND);
        } catch (JMSException e) {
            LOG.error("Could not parse command", e);
            return;
        }
        if (command == null) {
            LOG.error("Received null command");
            return;
        }

        Domain domain = null;
        try {
            String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            domain = domainService.getDomain(domainCode);
            domainContextProvider.setCurrentDomain(domainCode);
        } catch (JMSException e) {
            LOG.error("Could not get the domain", e);
            return;
        }

        commandService.executeCommand(command, domain, getCommandProperties(message));
    }

    /**
     * just extract all message properties (of type {@code String}) excepting Command and Domain
     *
     * @param msg JMS Message
     * @return map of properties
     */
    public static Map<String, String> getCommandProperties(Message msg) {
        HashMap<String, String> properties = new HashMap<>();
        try {
            Enumeration srcProperties = msg.getPropertyNames();
            while (srcProperties.hasMoreElements()) {
                String propertyName = (String) srcProperties.nextElement();
                if (!Command.COMMAND.equalsIgnoreCase(propertyName) && !MessageConstants.DOMAIN.equalsIgnoreCase(propertyName)
                        && msg.getObjectProperty(propertyName) instanceof String) {
                    properties.put(propertyName, msg.getStringProperty(propertyName));
                }
            }
        } catch (JMSException e) {
            LOG.error("An error occurred while trying to extract message properties: ", e);
        }
        return properties;
    }

    /**
     * just extract all message properties (of type {@code String}) excepting Command and Domain
     *
     * @param messageProperties
     * @return
     */
    public static Map<String, String> getCommandProperties(Map<String, Object> messageProperties) {
        HashMap<String, String> properties = new HashMap<>();

        if (MapUtils.isNotEmpty(messageProperties)) {
            for (Map.Entry<String, Object> entry : messageProperties.entrySet()) {
                if (!Command.COMMAND.equalsIgnoreCase(entry.getKey()) && !MessageConstants.DOMAIN.equalsIgnoreCase(entry.getKey())
                        && messageProperties.get(entry.getKey()) instanceof String) {
                    properties.put(entry.getKey(), (String) messageProperties.get(entry.getKey()));
                }
            }
        }

        return properties;
    }
}

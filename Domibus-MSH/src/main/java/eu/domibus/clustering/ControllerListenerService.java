package eu.domibus.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.lang.management.ManagementFactory;
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
        String command;
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

        Domain domain;
        try {
            String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            domain = domainService.getDomain(domainCode);
            domainContextProvider.setCurrentDomain(domainCode);
        } catch (JMSException e) {
            LOG.error("Could not get the domain", e);
            return;
        }

        Map<String, String> commandProperties = getCommandProperties(message);

        if (!skipCommandSameServer(command, domain, commandProperties)) {
            commandService.executeCommand(command, domain, commandProperties);
        }

    }

    /**
     * just extract all message properties (of type {@code String}) excepting Command and Domain
     *
     * @param msg JMS Message
     * @return map of properties
     */
    protected Map<String, String> getCommandProperties(Message msg) {
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
     * Returns true if the commands is send to same server
     * @param command
     * @param domain
     * @param commandProperties
     * @return
     */
    protected boolean skipCommandSameServer(final String command, final Domain domain, Map<String, String> commandProperties) {
        String originServerName = commandProperties.get(CommandProperty.ORIGIN_SERVER);

        //execute the command
        if (StringUtils.isBlank(originServerName)) {
            return false;
        }

        final String serverName = ManagementFactory.getRuntimeMXBean().getName();

        if (serverName.equalsIgnoreCase(originServerName)) {
            LOG.info("Command [{}] for domain [{}] not executed as origin and actual server signature is the same [{}]", command, domain, serverName);
            return true;
        }
        return false;
    }


}
